package org.example.src.Entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MovieShow {
    private String theatreName;
    private String movieName;
    private String showTime;
    private Map<SeatType, ShowSeats> showSeats;

    public MovieShow(String theatreName, String movieName, String showTime, Map<SeatType, ShowSeats> showSeats) {
        this.theatreName = theatreName;
        this.movieName = movieName;
        this.showTime = showTime;
        this.showSeats = showSeats;
    }

    @Getter
    @Setter
    public static class ShowSeats {
        private SeatType seatType;
        private int price;
        private int seatsAvailable;
        private int totalSeats;

        public ShowSeats(SeatType seatType, int price, int seatsAvailable, int totalSeats){
            this.price = price;
            this.seatType = seatType;
            this.seatsAvailable = seatsAvailable;
            this.totalSeats = totalSeats;
        }
    }

    public enum SeatType {
        GOLD,
        PLATINUM
    }

}
