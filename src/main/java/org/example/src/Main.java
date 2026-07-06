package org.example.src;

import org.example.src.Entities.Booking;
import org.example.src.Entities.Movie;
import org.example.src.Entities.MovieShow;
import org.example.src.Entities.User;
import org.example.src.services.BookingService;
import org.example.src.services.MovieService;
import org.example.src.services.RatingService;
import org.example.src.services.SingleScreenThreatre;
import org.example.src.services.UserService;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Meesho movie booking platform");

        UserService userService = new UserService();
        MovieService movieService = new MovieService();
        RatingService ratingService = new RatingService();
        SingleScreenThreatre singleScreenThreatre = new SingleScreenThreatre();

        BookingService bookingService = new BookingService(userService, singleScreenThreatre);
        User user = userService.registerUser("8688749458","Pralove", User.Gender.MALE, "Bangalore");

//        System.out.println("name:" + user.getName() + "phone:" + user.getPhone() + "Gender:" + user.getGender() + "city:" + user.getCity());
        System.out.println(user.toString());
        singleScreenThreatre.registerTheatre("PVR Koramangala", "Bangalore");
        Movie movie =  movieService.addMovie("Movie1", "English");
        System.out.println(movie.toString());

        MovieShow.ShowSeats showSeat1 = singleScreenThreatre.addShowSeats(50, MovieShow.SeatType.GOLD, 250);
        List<MovieShow.ShowSeats> showSeatsList = new ArrayList<>(List.of(showSeat1));
//
        singleScreenThreatre.addShow("PVR Koramangala", "Movie1", "18:00", showSeatsList);
//
        Booking booking = bookingService.bookTicket("8688749458", "PVR Koramangala", "Movie1", "18:00", 2, MovieShow.SeatType.GOLD);
        System.out.println(booking.toString());
//
//        ratingService.addRating("phone1", "Movie1", 5, "Best movie");
//
//        singleScreenThreatre.getMovieShows( "PVR Koramangala");

    }
}


//register_user
//2. register_theatre
//3. add_show
//4. book_ticket
//5. rate_movie
//6. show_shows
//7. show_reviews
