package org.example.rateLimiterTierBased.tokenBucket;

public class TokenBucketMain {
    public static void main(String[] args) throws InterruptedException {

        TokenBucketService tokenBucketService = new TokenBucketService();

        User user = tokenBucketService.addUser("venky", User.Tier.PRO);
        tokenBucketService.addApi(User.Tier.PRO, "opus", 2);

        tokenBucketService.checkRateLimit(user.userId, "opus");
        tokenBucketService.checkRateLimit(user.userId, "opus");

        Thread thread = new Thread();
        thread.sleep(30000);
        tokenBucketService.checkRateLimit(user.userId, "opus");

    }
}
