package org.example.movieBookingPlatform.Entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
