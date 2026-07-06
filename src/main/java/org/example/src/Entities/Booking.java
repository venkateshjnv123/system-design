package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Booking {
    private String phoneNumber;
    private String theatreName;
    private String movieName;
    private String showTime;
    private int noOfSeats;
    private MovieShow.SeatType seatType;
    private int totalCost;
    private BookingStatus bookingStatus;
    private Long id;

    public Booking(String phoneNumber, String theatreName, String movieName,String showTime, int noOfSeats, MovieShow.SeatType seatType, int totalCost, BookingStatus bookingStatus, Long id){
        this.phoneNumber = phoneNumber;
        this.theatreName = theatreName;
        this.movieName = movieName;
        this.showTime = showTime;
        this.noOfSeats = noOfSeats;
        this.seatType = seatType;
        this.totalCost = totalCost;
        this.bookingStatus = bookingStatus;
        this.id = id;
    }

    public enum BookingStatus{
        CONFIRMED,
        CANCELLED
    }
}
