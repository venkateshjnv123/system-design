package org.example.src.services;

import org.example.src.Entities.Booking;
import org.example.src.Entities.MovieShow;
import org.example.src.Entities.Theatre;
import org.example.src.Entities.User;

import java.util.concurrent.ConcurrentHashMap;

public class BookingService {
    private UserService userService;
    private SingleScreenThreatre singleScreenThreatre;

    private ConcurrentHashMap<Long, Booking> bookings = new ConcurrentHashMap<>();
    private Long bookingId = 1l;

    private void incrementBookingId(){
        bookingId++;
    }

    public BookingService(UserService userService, SingleScreenThreatre singleScreenThreatre) {
        this.userService = userService;
        this.singleScreenThreatre = singleScreenThreatre;
    }

    public Booking bookTicket(String userPhoneNumber, String theatre_name, String movie_name, String show_time, int number_of_seats, MovieShow.SeatType seatType){
        User user = userService.getUser(userPhoneNumber);
        Theatre theatre = singleScreenThreatre.getTheatre(theatre_name);
        MovieShow movieShow = singleScreenThreatre.getMovieShow(show_time, theatre_name);

        if(movieShow.getShowSeats().get(seatType).getSeatsAvailable() < number_of_seats){
            throw new IllegalArgumentException("Not enough seats available");
        }

        movieShow.getShowSeats().get(seatType).setSeatsAvailable(movieShow.getShowSeats().get(seatType).getSeatsAvailable() - number_of_seats);
        int totalCost = movieShow.getShowSeats().get(seatType).getPrice() * number_of_seats ;
        Booking booking = new Booking(userPhoneNumber, theatre_name, movie_name, show_time, number_of_seats, seatType, totalCost, Booking.BookingStatus.CONFIRMED, bookingId);
        incrementBookingId();
        return booking;
    }

}
