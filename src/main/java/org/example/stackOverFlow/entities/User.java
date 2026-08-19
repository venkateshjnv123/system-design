package org.example.stackOverFlow.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@ToString
public class User {
    public String id;
    public String name;
    public AtomicInteger reputation;

    public User(String name){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.reputation = new AtomicInteger(0);
    }

    public void updateReputation(int val){
        this.reputation.addAndGet(val);
    }
}
