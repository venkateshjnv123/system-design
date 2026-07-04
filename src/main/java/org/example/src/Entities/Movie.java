package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Movie {
    String name;
    String language;

    public Movie(String name, String language){
        this.name = name;
        this.language = language;
    }
}
