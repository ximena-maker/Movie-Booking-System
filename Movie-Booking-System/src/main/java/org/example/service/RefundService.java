package org.example.service;

import org.example.data.DataStore;
import org.example.model.Booking;
import org.example.model.BookingStatus;

import java.util.Map;

public class RefundService {
    private final DataStore db;
    public RefundService(DataStore db) { this.db = db; }

    public boolean refund(String bookingId) {
        Booking b = db.bookings.get(bookingId);
        if (b == null) return false;
        if (b.status() != BookingStatus.PAID) return false;

        // 釋放座位
        Map<String, Boolean> seats = db.seatMap.get(b.showtimeId());
        if (seats != null) {
            for (String seatId : b.seatIds()) seats.put(seatId, true);
        }

        Booking refunded = new Booking(
                b.bookingId(), b.userId(), b.showtimeId(), b.seatIds(), b.ticketType(),
                b.mealCombo(), b.discountAmount(), b.totalPrice(),
                BookingStatus.REFUNDED, b.createdAtMillis()
        );
        db.bookings.put(bookingId, refunded);
        return true;
    }
}

