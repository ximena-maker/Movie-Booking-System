package org.example.service;

import org.example.data.DataStore;
import org.example.model.Movie;
import org.example.model.Theater;

public class RecommendationService {
    private final DataStore db;
    public RecommendationService(DataStore db) { this.db = db; }

    public Theater nearestTheater(double x, double y) {
        return db.theaters.values().stream()
                .min((a,b) -> Double.compare(dist2(a,x,y), dist2(b,x,y)))
                .orElseThrow();
    }

    public Movie hottestMovie() {
        String bestId = db.popularity.entrySet().stream()
                .max((a,b) -> Integer.compare(a.getValue(), b.getValue()))
                .map(e -> e.getKey())
                .orElse("M1");
        return db.movies.get(bestId);
    }

    private double dist2(Theater t, double x, double y) {
        double dx = t.x() - x, dy = t.y() - y;
        return dx*dx + dy*dy;
    }
}
