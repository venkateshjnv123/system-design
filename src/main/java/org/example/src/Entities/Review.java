package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Review {
    String userPhoneNumber;
    String movieName;
    int rating;
    String comment;
    Long id;

    public Review(String userPhoneNumber, String movieName, int rating, String comment, Long id){
        this.userPhoneNumber = userPhoneNumber;
        this.movieName = movieName;
        this.rating = rating;
        this.comment = comment;
        this.id = id;
    }
}
