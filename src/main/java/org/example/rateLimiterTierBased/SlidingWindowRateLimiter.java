package org.example.rateLimiterTierBased;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SLIDING WINDOW LOG ALGORITHM — Tier-Based Rate Limiter
 *
 * Data Model:
 *   RateLimitConfig  : (apiName, tier)       → limit (per minute)
 *   UserRequestLog   : (userId, apiName)      → sorted set of timestamps  [Redis: ZADD]
 *   Users            : userId                 → { username, tier }
 *
 * Storage:
 *   - RateLimitConfig  → in-memory map (rarely changes); warm from DB on startup
 *   - UserRequestLog   → Redis Sorted Set  key=(userId:apiName), score=epochMs
 *   - Users            → Redis cache (TTL ~5 min); fallback to DB
 *
 * Thread Safety:
 *   - ZADD + ZCOUNT + ZREMRANGEBYSCORE wrapped in a Redis Lua script → atomic, no separate lock needed
 *   - For single-instance: ConcurrentHashMap + synchronized block on (userId+apiName) key
 *
 * Complexity (per request):
 *   Time  : O(log N + M) — N = entries in sorted set for this window, M = evicted stale entries
 *           ZREMRANGEBYSCORE is O(log N + M), ZADD is O(log N), ZCARD is O(1)
 *   Space : O(N) per (userId, apiName) — stores one timestamp per request in the window
 *           Bounded by tier limit × window size; auto-cleaned by ZREMRANGEBYSCORE + PEXPIRE
 *
 * Redis / Rate-Limiter Unavailability — Three Strategies:
 *
 *   1. FAIL OPEN (allow requests through)
 *      - If Redis throws an exception, catch it and ALLOW the request
 *      - Prioritizes availability over correctness
 *      - Risk: bad actors can abuse the API during outage window
 *      - Suitable for: non-critical public APIs, read-heavy endpoints
 *      - Implementation: wrap redis.executeAtomicSlidingWindow() in try-catch → return true on exception
 *
 *   2. FAIL CLOSED (deny all requests)
 *      - If Redis is down, return 503 Service Unavailable (not 429)
 *      - Prioritizes correctness over availability
 *      - Risk: legitimate users are blocked during outage
 *      - Suitable for: financial APIs, billing systems, security-critical endpoints
 *      - Implementation: let RedisException propagate → API Gateway catches → returns 503
 *
 *   3. FALLBACK TO IN-MEMORY (approximate limiting)
 *      - If Redis is down, fall back to local ConcurrentHashMap-based limiter per instance
 *      - Each instance enforces its own limit independently
 *      - Risk: with K instances, up to K × limit requests can slip through cluster-wide
 *      - Suitable for: systems where slight over-admission is acceptable (e.g. 2× limit briefly)
 *      - Implementation: catch RedisException → call checkRateLimitSingleInstance(userId, apiName)
 *
 *   Recommendation for this system:
 *      Use FAIL OPEN with circuit breaker + alerting — most subscription APIs prefer availability.
 *      Log all bypassed requests during outage for post-hoc billing correction.
 */
public class SlidingWindowRateLimiter {

    // -------------------------------------------------------------------------
    // Data structures (pseudo — in real impl these would be Redis clients / DAOs)
    // -------------------------------------------------------------------------

    // RateLimitConfig: key = "apiName:tier", value = max requests per minute
    // e.g. "PUT /opus:pro" -> 10,  "PUT /opus:max" -> 20,  "PUT /opus:free" -> 5
    Map<String, Integer> rateLimitConfig = new ConcurrentHashMap<>();

    // UserRequestLog: key = "userId:apiName", value = Redis Sorted Set of timestamps
    // Represented here as a pseudo Redis client
    RedisClient redis = new RedisClient(); // pseudo

    // Users cache: key = userId, value = UserDetails
    Map<String, UserDetails> usersCache = new ConcurrentHashMap<>();

    // Per-key lock map (single-instance fallback — use Redis Redlock in distributed setup)
    Map<String, Object> lockMap = new ConcurrentHashMap<>();

    static final long WINDOW_MS = 60_000L; // 1 minute sliding window

    // -------------------------------------------------------------------------
    // MAIN ENTRY POINT
    // -------------------------------------------------------------------------

    /**
     * checkRateLimit — called by API Gateway before forwarding request to service.
     *
     * @throws RateLimitExceededException (HTTP 429) if user has exhausted their tier quota
     */
    public void checkRateLimit(String userId, String apiName) {

        // 1. Get user tier — Redis cache first, fallback to DB
        UserDetails userDetails = getUserDetails(userId);
        String tier = userDetails.getTier(); // "free" | "pro" | "max"

        // 2. Get rate limit for this (api, tier) pair — in-memory config
        int rateLimit = getRateLimit(apiName, tier);

        // 3. Distributed atomic check-and-insert via Redis Lua script
        //    (replaces lock.lock() + check + insert + lock.unlock())
        //
        //    Lua script (atomic on Redis single thread):
        //      local key   = KEYS[1]               -- "userId:apiName"
        //      local now   = tonumber(ARGV[1])      -- current epoch ms
        //      local window= tonumber(ARGV[2])      -- 60000 ms
        //      local limit = tonumber(ARGV[3])      -- tier rate limit
        //
        //      redis.call('ZREMRANGEBYSCORE', key, 0, now - window)   -- evict old entries
        //      local count = redis.call('ZCARD', key)                 -- count in window
        //      if count >= limit then
        //        return 0                                              -- DENY
        //      end
        //      redis.call('ZADD', key, now, now .. '-' .. math.random()) -- log request
        //      redis.call('PEXPIRE', key, window)                     -- auto-cleanup TTL
        //      return 1                                               -- ALLOW

        String redisKey = userId + ":" + apiName;
        long now = System.currentTimeMillis();

        boolean allowed = redis.executeAtomicSlidingWindow(redisKey, now, WINDOW_MS, rateLimit);

        if (!allowed) {
            long retryAfterMs = redis.getWindowResetMs(redisKey, now, WINDOW_MS); // oldest entry + window - now
            throw new RateLimitExceededException(
                "Rate limit exceeded for tier: " + tier,
                429,
                retryAfterMs / 1000 // Retry-After header in seconds
            );
        }

        // Request allowed — proceed to downstream service
    }

    // -------------------------------------------------------------------------
    // SINGLE-INSTANCE ALTERNATIVE (no Redis — for interviews without distributed req)
    // -------------------------------------------------------------------------

    /**
     * checkRateLimitSingleInstance — uses in-process lock + ConcurrentHashMap.
     * NOT suitable for multi-instance deployments.
     */
    public void checkRateLimitSingleInstance(String userId, String apiName) {

        UserDetails userDetails = getUserDetails(userId);
        int rateLimit = getRateLimit(apiName, userDetails.getTier());

        String lockKey = userId + ":" + apiName;
        Object lock = lockMap.computeIfAbsent(lockKey, k -> new Object());

        synchronized (lock) {
            // fetchRequestsForUser: count log entries within [now-60s, now], evict older ones
            int requestsTillNow = fetchRequestsForUser(userId, apiName);

            if (requestsTillNow >= rateLimit) {
                throw new RateLimitExceededException("Rate limit exceeded", 429, 60);
            }

            // Log this request timestamp
            redis.zadd(lockKey, System.currentTimeMillis());
        }
        // lock releases here — even if exception thrown (synchronized auto-releases)
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * fetchRequestsForUser — count requests in sliding window [now-60s, now].
     * Evicts stale entries older than 1 minute (keeps sorted set small).
     *
     * Redis equivalent:
     *   ZREMRANGEBYSCORE key 0 (now - 60000)
     *   ZCARD key
     */
    private int fetchRequestsForUser(String userId, String apiName) {
        String key = userId + ":" + apiName;
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;

        redis.zremrangeByScore(key, 0, windowStart); // evict entries older than 1 minute
        return redis.zcard(key);                     // count remaining in window
    }

    /**
     * getRateLimit — in-memory config lookup (loaded from DB at startup).
     * No per-request DB hit needed; config changes are rare.
     */
    private int getRateLimit(String apiName, String tier) {
        String configKey = apiName + ":" + tier;
        Integer limit = rateLimitConfig.get(configKey);
        if (limit == null) {
            throw new IllegalArgumentException("No rate limit config for: " + configKey);
        }
        return limit;
    }

    /**
     * getUserDetails — Redis cache (TTL 5 min) → DB fallback.
     */
    private UserDetails getUserDetails(String userId) {
        // Check Redis cache first
        UserDetails cached = redis.getObject(userId, UserDetails.class);
        if (cached != null) return cached;

        // Fallback to DB
        UserDetails userDetails = usersDB.findById(userId); // pseudo DB call
        redis.setWithTTL(userId, userDetails, 300); // cache for 5 min
        return userDetails;
    }

    // -------------------------------------------------------------------------
    // STUB CLASSES (would be real models / Redis client in production)
    // -------------------------------------------------------------------------

    static class UserDetails {
        private String userId;
        private String username;
        private String tier; // "free" | "pro" | "max"

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

    // Pseudo stubs — represent Redis client and DB in a real impl
    static class RedisClient {
        boolean executeAtomicSlidingWindow(String key, long now, long windowMs, int limit) { return true; }
        long getWindowResetMs(String key, long now, long windowMs) { return 0; }
        void zadd(String key, long score) {}
        void zremrangeByScore(String key, long min, long max) {}
        int zcard(String key) { return 0; }
        <T> T getObject(String key, Class<T> type) { return null; }
        void setWithTTL(String key, Object value, int ttlSeconds) {}
    }

    static class UsersDB {
        UserDetails findById(String userId) { return new UserDetails(); }
    }

    UsersDB usersDB = new UsersDB();
}
