package org.example.trainticketplatform.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class User {
   public String id;
   public String name;
   public String phone; // unique Identifier

    public User(String name, String phone){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
    }

}
