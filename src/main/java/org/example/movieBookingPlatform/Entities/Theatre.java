package org.example.movieBookingPlatform.Entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Theatre {
    private String name;
    private String city;

    public Theatre(String name, String city){
        this.name = name;
        this.city = city;
    }
}
