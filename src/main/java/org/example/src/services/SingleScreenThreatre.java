package org.example.src.services;

import org.example.src.Entities.MovieShow;
import org.example.src.Entities.Theatre;
import org.example.src.Entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SingleScreenThreatre implements ThreatreService {
    private ConcurrentHashMap<String, Theatre> theatres = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<MovieShow>> movieShows = new ConcurrentHashMap<>();

    @Override
    public void registerTheatre(String name, String city) {
        if (theatres.containsKey(name)) {
            throw new IllegalArgumentException("Theatre already exists");
        }
        theatres.put(name, new Theatre(name, city));
    }

    @Override
    public MovieShow.ShowSeats addShowSeats(int totalSeats, MovieShow.SeatType seatType, int ticketPrice) {
        return new MovieShow.ShowSeats(seatType, ticketPrice, totalSeats ,totalSeats);
    }

    @Override
    public void addShow(String name, String movieName, String startTime, List<MovieShow.ShowSeats> showSeats) {
        if(!theatres.containsKey(name)) {
            throw new IllegalArgumentException("Theatre doesn't exists");
        }

        Map<MovieShow.SeatType, MovieShow.ShowSeats> showSeatsMap = showSeats.stream()
            .collect(Collectors.toMap(MovieShow.ShowSeats::getSeatType, Function.identity()));

        MovieShow movieShow = new MovieShow(name, movieName, startTime, showSeatsMap);

        if(movieShows.containsKey(name)) {
            movieShows.get(name).add(movieShow);
        }

        System.out.println("Show added successfully");
    }

    @Override
    public Theatre getTheatre(String name){
        if (!theatres.containsKey(name)) {
            throw new IllegalArgumentException("Theatre doesn't exists");
        }
        return theatres.get(name);
    }

    public MovieShow getMovieShow(String showTime, String theatreName){
        return movieShows.get(theatreName).stream().filter(movieShow -> movieShow.getShowTime().equals(showTime)).findFirst().get();
    }

    public List<MovieShow> getMovieShows(String theatreName){
        return movieShows.get(theatreName);
    }
}
