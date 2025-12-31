package org.example.model;

import java.time.LocalDateTime;

public record Showtime(
        String showtimeId,
        String theaterId,
        String movieId,
        LocalDateTime startTime,
        int basePrice
) {}