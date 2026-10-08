package org.example.movieBookingPlatform.services;

import org.example.movieBookingPlatform.Entities.Booking;
import org.example.movieBookingPlatform.Entities.MovieShow;
import org.example.movieBookingPlatform.Entities.Theatre;
import org.example.movieBookingPlatform.Entities.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class BookingService {
    private UserService userService;
    private SingleScreenThreatre singleScreenThreatre;
    private final ReentrantLock lock = new ReentrantLock();

    private ConcurrentHashMap<Integer, Booking> bookings = new ConcurrentHashMap<>();
    private AtomicInteger bookingId = new AtomicInteger(0);

    private void incrementBookingId(){
        bookingId.getAndIncrement();
    }

    public BookingService(UserService userService, SingleScreenThreatre singleScreenThreatre) {
        this.userService = userService;
        this.singleScreenThreatre = singleScreenThreatre;
    }

    public Booking bookTicket(String userPhoneNumber, String theatre_name, String movie_name, String show_time, int number_of_seats, MovieShow.SeatType seatType){
        User user = userService.getUser(userPhoneNumber);
        Theatre theatre = singleScreenThreatre.getTheatre(theatre_name);
        MovieShow movieShow = singleScreenThreatre.getMovieShow(show_time, theatre_name);

        if(!tryBookSeats(number_of_seats, movieShow, seatType)){
            throw new IllegalArgumentException("Not enough seats available");
        }

        movieShow.getShowSeats().get(seatType).setSeatsAvailable(movieShow.getShowSeats().get(seatType).getSeatsAvailable() - number_of_seats);
        int totalCost = movieShow.getShowSeats().get(seatType).getPrice() * number_of_seats ;
        Booking booking = new Booking(userPhoneNumber, theatre_name, movie_name, show_time, number_of_seats, seatType, totalCost, Booking.BookingStatus.CONFIRMED, bookingId.get());
        incrementBookingId();
        bookings.put(bookingId.getAndIncrement(), booking);
        return booking;
    }

    public boolean tryBookSeats(int count, MovieShow movieShow, MovieShow.SeatType seatType){
        lock.lock();
        try {
            if(movieShow.getShowSeats().get(seatType).getSeatsAvailable() < count) return false;
            movieShow.getShowSeats().get(seatType).setSeatsAvailable(movieShow.getShowSeats().get(seatType).getSeatsAvailable() - count);
            return true;
        } finally {
            lock.unlock();
        }
    }
}
