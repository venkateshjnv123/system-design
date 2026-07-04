package org.example.src.services;

import org.example.src.Entities.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public User registerUser(String phone, String name, User.Gender gender, String city){
        if (users.containsKey(phone)) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User(name, gender, phone, city);
        users.put(phone, user);
        return user;
    }

    public User getUser(String phone){
        if (!users.containsKey(phone)) {
            throw new IllegalArgumentException("User doesn't exists");
        }
        return users.get(phone);
    }

}
