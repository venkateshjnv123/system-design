package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {
    private String name;
    private Gender gender;
    private String phone;
    private String city;

    public enum Gender{
        MALE,
        FEMALE,
        OTHERS
    }

    public User(String name, Gender gender, String phone, String city){
        this.city = city;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
    }
}
