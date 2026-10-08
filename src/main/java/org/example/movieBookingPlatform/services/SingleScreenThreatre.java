package org.example.movieBookingPlatform.services;

import org.example.movieBookingPlatform.Entities.MovieShow;
import org.example.movieBookingPlatform.Entities.Theatre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
        Theatre theatre = new Theatre(name, city);
        theatres.put(name, theatre );
        System.out.println(theatre);
    }

    @Override
    public MovieShow.ShowSeats addShowSeats(int totalSeats, MovieShow.SeatType seatType, int ticketPrice) {
        validate(totalSeats, ticketPrice);
        return new MovieShow.ShowSeats(seatType, ticketPrice, totalSeats ,totalSeats);
    }

    public void validate(int totalSeats, int ticketPrice){
        if(totalSeats < 0 || ticketPrice < 0){
            throw new IllegalArgumentException("Total seats and ticket price should be positive");
        }
    }

    @Override
    public void addShow(String name, String movieName, String startTime, List<MovieShow.ShowSeats> showSeats) {
        if(!theatres.containsKey(name)) {
            throw new IllegalArgumentException("Theatre doesn't exists");
        }

        Map<MovieShow.SeatType, MovieShow.ShowSeats> showSeatsMap = showSeats.stream()
            .collect(Collectors.toMap(MovieShow.ShowSeats::getSeatType, Function.identity()));

        MovieShow movieShow = new MovieShow(name, movieName, startTime, showSeatsMap);

        validateShow(name, movieShow);
        movieShows.putIfAbsent(name, new ArrayList<>());
        movieShows.get(name).add(movieShow);

        System.out.println("Show added successfully");
    }

    public void validateShow(String name, MovieShow show){
        if(movieShows.containsKey(name)){
            movieShows.get(name).stream().filter(movieShow -> movieShow.getShowTime().equals(show.getShowTime())).findFirst().ifPresent(movieShow -> {
                throw new IllegalArgumentException("Show timing collide with another show");
            });
        }
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
