package org.example.src.services;

import org.example.src.Entities.Movie;

import java.util.concurrent.ConcurrentHashMap;

public class MovieService {
    private ConcurrentHashMap<String, Movie> movies = new ConcurrentHashMap<>();

    public Movie addMovie(String name, String language){
        if (movies.containsKey(name)) {
            throw new IllegalArgumentException("Movie already exists");
        }
        movies.put(name, new Movie(name, language));
        return movies.get(name);
    }

    public Movie getMovie(String name){
        if (!movies.containsKey(name)) {
            throw new IllegalArgumentException("Movie doesn't exists");
        }
        return movies.get(name);
    }
}
