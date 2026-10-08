package org.example.rateLimiterTierBased;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TOKEN BUCKET ALGORITHM — Tier-Based Rate Limiter
 *
 * Concept:
 *   Each (userId, apiName) pair gets a "bucket" with a max capacity = tier limit.
 *   Tokens refill at a steady rate (e.g. 10 tokens/min for FREE tier).
 *   Each request consumes 1 token. If bucket is empty → DENY (429).
 *
 * Advantage over Sliding Window Log:
 *   - Allows SHORT BURSTS — user can consume all tokens at once if bucket is full.
 *   - Sliding window smooths requests evenly; token bucket allows burst then throttle.
 *   - More memory-efficient: O(1) state per (userId, apiName) vs O(N) log entries.
 *
 * Data Model:
 *   TokenBucket: { tokens: double, lastRefillTime: long }
 *     key = "userId:apiName"
 *     Stored in Redis Hash or in-memory ConcurrentHashMap
 *
 * Thread Safety:
 *   - Single-instance: synchronized on bucket object
 *   - Distributed: Redis Hash + Lua script for atomic read-modify-write
 *
 * Complexity (per request):
 *   Time  : O(1) — refill is pure arithmetic; Redis HMGET/HMSET are O(1)
 *   Space : O(1) per (userId, apiName) — only stores {tokens, lastRefillTime} regardless of request volume
 *           Far more memory-efficient than Sliding Window Log at high traffic
 *
 * Redis / Rate-Limiter Unavailability — Three Strategies:
 *
 *   1. FAIL OPEN (allow requests through)
 *      - Catch RedisException in checkRateLimitDistributed() and ALLOW the request
 *      - Prioritizes availability over correctness
 *      - Risk: quota enforcement pauses during outage; abuse possible
 *      - Suitable for: non-critical public APIs, read-heavy endpoints
 *
 *   2. FAIL CLOSED (deny all requests)
 *      - Let RedisException propagate → API Gateway returns 503 Service Unavailable
 *      - Note: return 503 (service down), NOT 429 (rate limited) — different semantics
 *      - Prioritizes correctness over availability
 *      - Suitable for: financial APIs, metered billing, security-critical endpoints
 *
 *   3. FALLBACK TO IN-MEMORY (approximate limiting)
 *      - Catch RedisException → fall back to local in-memory bucketStore (ConcurrentHashMap)
 *      - Each service instance enforces limit independently → up to K×limit requests cluster-wide
 *      - Token Bucket is particularly well-suited for this fallback: O(1) state, simple arithmetic
 *      - Suitable for: systems tolerant of brief over-admission (e.g. allow 2× limit for < 30s)
 *
 *   Recommendation for this system:
 *      Fail Open + in-memory fallback is the practical choice for subscription APIs.
 *      Token Bucket's O(1) state makes the in-memory fallback lightweight and fast.
 *      Alert on Redis unavailability; log bypass events for billing reconciliation.
 *
 * Tier Config (tokens per minute = bucket capacity):
 *   PUT /opus  | free=5,  pro=10, max=20
 *   PUT /sonnet| free=10, pro=20, max=50
 */
public class TokenBucketRateLimiter {

    // -------------------------------------------------------------------------
    // Data structures
    // -------------------------------------------------------------------------

    // RateLimitConfig: key = "apiName:tier", value = max tokens (= refill rate per minute)
    Map<String, Integer> rateLimitConfig = new ConcurrentHashMap<>();

    // Token buckets: key = "userId:apiName"
    // In distributed setup → stored in Redis Hash with fields: tokens, lastRefillTime
    Map<String, TokenBucket> bucketStore = new ConcurrentHashMap<>();

    // Users cache: Redis → DB fallback (same as sliding window)
    RedisClient redis = new RedisClient();
    UsersDB usersDB = new UsersDB();

    static final long WINDOW_MS = 60_000L; // refill window = 1 minute

    // -------------------------------------------------------------------------
    // MAIN ENTRY POINT
    // -------------------------------------------------------------------------

    /**
     * checkRateLimit — called by API Gateway before forwarding to service.
     *
     * @throws RateLimitExceededException (HTTP 429) if bucket is empty
     */
    public void checkRateLimit(String userId, String apiName) {

        // 1. Get user tier — Redis cache first, fallback to DB
        UserDetails userDetails = getUserDetails(userId);
        String tier = userDetails.getTier(); // "free" | "pro" | "max"

        // 2. Get bucket capacity for this (api, tier) pair
        int capacity = getRateLimit(apiName, tier); // e.g. 10 for pro/opus

        // 3. Get or create bucket for this (userId, apiName)
        String bucketKey = userId + ":" + apiName;
        TokenBucket bucket = bucketStore.computeIfAbsent(
            bucketKey,
            k -> new TokenBucket(capacity, System.currentTimeMillis())
        );

        // 4. Atomic consume — synchronized for single-instance
        //    For distributed: Redis Lua script (see below)
        synchronized (bucket) {
            refillBucket(bucket, capacity);

            if (bucket.tokens < 1) {
                // Calculate when next token will be available
                double tokensPerMs = (double) capacity / WINDOW_MS;
                long retryAfterMs = (long) ((1 - bucket.tokens) / tokensPerMs);
                throw new RateLimitExceededException(
                    "Rate limit exceeded for tier: " + tier,
                    429,
                    retryAfterMs / 1000 + 1 // Retry-After in seconds
                );
            }

            bucket.tokens -= 1; // consume one token
        }

        // Request allowed — proceed to downstream service
    }

    // -------------------------------------------------------------------------
    // DISTRIBUTED ALTERNATIVE (Redis Lua script)
    // -------------------------------------------------------------------------

    /**
     * checkRateLimitDistributed — uses Redis Lua for atomic token bucket.
     *
     * Lua script (runs atomically on Redis):
     *   local key      = KEYS[1]                     -- "userId:apiName"
     *   local capacity = tonumber(ARGV[1])            -- max tokens
     *   local now      = tonumber(ARGV[2])            -- current epoch ms
     *   local window   = tonumber(ARGV[3])            -- refill window in ms (60000)
     *
     *   local data = redis.call('HMGET', key, 'tokens', 'lastRefill')
     *   local tokens    = tonumber(data[1]) or capacity
     *   local lastRefill= tonumber(data[2]) or now
     *
     *   -- Refill proportional to time elapsed
     *   local elapsed    = now - lastRefill
     *   local refillRate = capacity / window           -- tokens per ms
     *   tokens = math.min(capacity, tokens + elapsed * refillRate)
     *
     *   if tokens < 1 then
     *     return 0                                     -- DENY
     *   end
     *
     *   tokens = tokens - 1                           -- consume token
     *   redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now)
     *   redis.call('PEXPIRE', key, window)            -- auto-cleanup
     *   return 1                                      -- ALLOW
     */
    public void checkRateLimitDistributed(String userId, String apiName) {
        UserDetails userDetails = getUserDetails(userId);
        int capacity = getRateLimit(apiName, userDetails.getTier());

        String bucketKey = userId + ":" + apiName;
        long now = System.currentTimeMillis();

        boolean allowed = redis.executeAtomicTokenBucket(bucketKey, capacity, now, WINDOW_MS);

        if (!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded", 429, 60);
        }
    }

    // -------------------------------------------------------------------------
    // REFILL LOGIC
    // -------------------------------------------------------------------------

    /**
     * refillBucket — adds tokens proportional to time elapsed since last refill.
     * Called inside synchronized block — no separate lock needed.
     *
     * Example: capacity=10, windowMs=60000 → refillRate = 10/60000 tokens/ms
     *   If 30 seconds have passed → add 5 tokens (up to max capacity)
     */
    private void refillBucket(TokenBucket bucket, int capacity) {
        long now = System.currentTimeMillis();
        long elapsed = now - bucket.lastRefillTime;

        double refillRate = (double) capacity / WINDOW_MS; // tokens per ms
        double tokensToAdd = elapsed * refillRate;

        bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
        bucket.lastRefillTime = now;
    }

    // -------------------------------------------------------------------------
    // HELPERS (same as SlidingWindowRateLimiter)
    // -------------------------------------------------------------------------

    private int getRateLimit(String apiName, String tier) {
        String configKey = apiName + ":" + tier;
        Integer limit = rateLimitConfig.get(configKey);
        if (limit == null) {
            throw new IllegalArgumentException("No rate limit config for: " + configKey);
        }
        return limit;
    }

    private UserDetails getUserDetails(String userId) {
        UserDetails cached = redis.getObject(userId, UserDetails.class);
        if (cached != null) return cached;

        UserDetails userDetails = usersDB.findById(userId);
        redis.setWithTTL(userId, userDetails, 300);
        return userDetails;
    }

    // -------------------------------------------------------------------------
    // COMPARISON: Token Bucket vs Sliding Window Log
    // -------------------------------------------------------------------------
    //
    //  | Aspect              | Sliding Window Log         | Token Bucket              |
    //  |---------------------|----------------------------|---------------------------|
    //  | Burst handling      | No — smoothly distributed  | Yes — burst allowed       |
    //  | Memory              | O(N) — stores each request | O(1) — just tokens+time   |
    //  | Accuracy            | Exact per-request tracking | Approximate (float math)  |
    //  | Redis data struct   | Sorted Set (ZADD/ZCOUNT)   | Hash (HMGET/HMSET)        |
    //  | Use case            | Strict API quota           | Bursty traffic (CDN, etc.)|
    //
    // For this problem (API subscription tiers with strict per-minute limits):
    //   → Sliding Window Log is more appropriate
    //   → Token Bucket better for scenarios where brief bursts are acceptable

    // -------------------------------------------------------------------------
    // STUB CLASSES
    // -------------------------------------------------------------------------

    static class TokenBucket {
        double tokens;       // current token count (double to support fractional refill)
        long lastRefillTime; // epoch ms of last refill

        TokenBucket(int capacity, long now) {
            this.tokens = capacity; // starts full
            this.lastRefillTime = now;
        }
    }

    static class UserDetails {
        private String userId;
        private String username;
        private String tier;

        public String getTier() { return tier; }
    }

    static class RateLimitExceededException extends RuntimeException {
        int httpStatus;
        long retryAfterSeconds;

        RateLimitExceededException(String msg, int status, long retryAfter) {
            super(msg);
            this.httpStatus = status;
            this.retryAfterSeconds = retryAfter;
        }
    }

    static class RedisClient {
        boolean executeAtomicTokenBucket(String key, int capacity, long now, long windowMs) { return true; }
        <T> T getObject(String key, Class<T> type) { return null; }
        void setWithTTL(String key, Object value, int ttlSeconds) {}
    }

    static class UsersDB {
        UserDetails findById(String userId) { return new UserDetails(); }
    }
}
