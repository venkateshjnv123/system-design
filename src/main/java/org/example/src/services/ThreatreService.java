package org.example.src.services;

import org.example.src.Entities.MovieShow;
import org.example.src.Entities.Theatre;

import java.util.List;

public interface ThreatreService {
   void registerTheatre(String name, String city);
   MovieShow.ShowSeats addShowSeats(int totalSeats, MovieShow.SeatType seatType,int ticketPrice);
   void addShow(String name, String movieName, String startTime, List<MovieShow.ShowSeats> showSeats);
   Theatre getTheatre(String name);
}
