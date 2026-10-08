package org.example.rateLimiterTierBased.tokenBucket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketService {
    Map<String, Integer> tokenBucketCapacity = new ConcurrentHashMap<>();
    Map<String, TokenBucket> tokenBucketStore = new ConcurrentHashMap<>();
    Map<String, User> userDetails = new ConcurrentHashMap<>();
    public static final long WINDOW_MS = 60_000L;

    public void checkRateLimit(String userId, String apiName){
        User user = userDetails.get(userId);
        if(user == null){
            throw new IllegalArgumentException("User not found");
        }

        String tokenBucketKey = user.getTier().toString() + ":" + apiName;
        int capacity = tokenBucketCapacity.get(tokenBucketKey);

        String userStoreKey = userId + ":" + apiName;

        TokenBucket tokenBucket = tokenBucketStore.get(userStoreKey);
        if(tokenBucket == null){
            tokenBucket = new TokenBucket(capacity, System.currentTimeMillis());
            tokenBucketStore.put(userStoreKey, tokenBucket);
        }

        synchronized (tokenBucket) {
            refillBucket(tokenBucket, capacity);
            System.out.println(tokenBucket.tokens);
            if(tokenBucket.tokens < 1) {
                throw new RuntimeException("Rate limit exceeded");
            }

            tokenBucket.tokens -= 1;

            System.out.println("request passed");

        }
    }

    public void refillBucket(TokenBucket tokenBucket, int capacity){
        long now = System.currentTimeMillis();
        long elapsedTime = now - tokenBucket.lastRefillTime;

        double refillToken = (double) capacity / WINDOW_MS;
        double tokensToAdd = refillToken * elapsedTime;

        tokenBucket.tokens = Math.min(capacity, (int) tokenBucket.tokens + tokensToAdd);
        tokenBucket.lastRefillTime = now;
    }

    public User addUser(String userName, User.Tier tier) {
        User user = new User(userName, tier);
        userDetails.put(user.getUserId(), user);
        return user;
    }

    public void addApi(User.Tier tier, String apiName, int capacity) {
        String tokenBucketKey = tier.toString() + ":" + apiName;
        tokenBucketCapacity.put(tokenBucketKey, capacity);
    }

    public class TokenBucket {
        double tokens;
        long lastRefillTime;

        public TokenBucket(double tokens, long lastRefillTime ){
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}
