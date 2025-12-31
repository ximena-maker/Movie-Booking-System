package org.example.service;

import org.example.data.DataStore;
import org.example.model.Movie;
import org.example.model.Showtime;

import java.util.List;

public class SearchService {
    private final DataStore db;
    public SearchService(DataStore db) { this.db = db; }

    public List<Movie> searchMoviesByTitle(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        return db.movies.values().stream()
                .filter(m -> m.title().toLowerCase().contains(kw))
                .toList();
    }

    public List<Showtime> listShowtimesByMovie(String movieId) {
        return db.showtimes.values().stream()
                .filter(s -> s.movieId().equals(movieId))
                .toList();
    }
}
