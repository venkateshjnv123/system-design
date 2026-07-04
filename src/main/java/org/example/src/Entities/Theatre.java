package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Theatre {
    private String name;
    private String city;

    public Theatre(String name, String city){
        this.name = name;
        this.city = city;
    }
}
