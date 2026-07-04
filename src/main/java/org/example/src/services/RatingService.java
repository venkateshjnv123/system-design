package org.example.src.services;

import org.example.src.Entities.Review;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RatingService {
    private ConcurrentHashMap<Long, Review> reviews = new ConcurrentHashMap<>();
    private Long id = 0L;

    private void incrementId(){
        id++;
    }

    public void addRating(String userPhoneNumber, String movie_name, int rating, String comment){
        Review review = new Review(userPhoneNumber, movie_name, rating, comment, id);
        id++;
        reviews.put(review.getId(), review);
    }

    public List<Review> showReviews(String movieName){
        return reviews.values().stream().filter(review -> review.getMovieName().equals(movieName)).collect(Collectors.toList());
    }

//    public int avgRating(String movieName){
//        int avgRating = 0;
//
//        List<Reviews> movieReviews
//
//        for(Review review: reviews.values()){
//
//        }
//    }

}
