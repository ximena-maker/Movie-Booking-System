package org.example.model;

import java.util.List;

public record Booking(
        String bookingId,
        String userId,
        String showtimeId,
        List<String> seatIds,
        TicketType ticketType,
        MealCombo mealCombo,
        Integer discountAmount,
        int totalPrice,
        BookingStatus status,
        long createdAtMillis
) {}
