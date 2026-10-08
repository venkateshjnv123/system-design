package org.example.movieBookingPlatform.services;

import org.example.movieBookingPlatform.Entities.Review;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RatingService {
    private ConcurrentHashMap<Long, Review> reviews = new ConcurrentHashMap<>();
    private Long id = 0L;

    public void addRating(String userPhoneNumber, String movie_name, int rating, String comment){
        validate(rating);
        validateMovie(movie_name, userPhoneNumber);
        Review review = new Review(userPhoneNumber, movie_name, rating, comment, id);
        id++;
        reviews.put(review.getId(), review);
    }

    public void validate(int rating){
        if(rating < 1 || rating > 5){
            throw new IllegalArgumentException("Rating should be between 1 and 5");
        }
    }

    public void validateMovie(String movie,String phone) {
        if(reviews.containsKey(phone)){
            reviews.values().stream().filter(review -> review.getMovieName().equals(movie)).findFirst().ifPresent(review -> {
                throw new IllegalArgumentException("User already rated this movie");
            });
        }
    }


    public List<Review> showReviews(String movieName){
        return reviews.values().stream().filter(review -> review.getMovieName().equals(movieName)).collect(Collectors.toList());
    }

    public double avgRating(String movieName){
        return reviews.values().stream().filter(review -> review.getMovieName().equals(movieName)).mapToInt(Review::getRating).average().orElse(0);
    }

}
