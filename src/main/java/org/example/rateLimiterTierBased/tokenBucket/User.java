package org.example.rateLimiterTierBased.tokenBucket;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class User {
    String userName;
    Tier tier;
    String userId;

    public User(String userName, Tier tier) {
        this.userName = userName;
        this.tier = tier;
        this.userId = UUID.randomUUID().toString();
    }

    public enum Tier {
        FREE,
        PRO,
        MAX
    }

}
