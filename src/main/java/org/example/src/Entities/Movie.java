package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Movie {
    String name;
    String language;

    public Movie(String name, String language){
        this.name = name;
        this.language = language;
    }
}
