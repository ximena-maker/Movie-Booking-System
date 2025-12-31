package org.example.service;

import org.example.data.DataStore;
import org.example.model.Showtime;

import java.util.List;

public class PriceCompareService {
    private final DataStore db;
    public PriceCompareService(DataStore db) { this.db = db; }

    public List<String> compareByMovie(String movieId) {
        return db.showtimes.values().stream()
                .filter(s -> s.movieId().equals(movieId))
                .sorted((a,b) -> Integer.compare(a.basePrice(), b.basePrice()))
                .map(s -> {
                    var th = db.theaters.get(s.theaterId());
                    return String.format("%s | %s | %s | $%d",
                            s.showtimeId(), th.name(), s.startTime(), s.basePrice());
                })
                .toList();
    }
}
