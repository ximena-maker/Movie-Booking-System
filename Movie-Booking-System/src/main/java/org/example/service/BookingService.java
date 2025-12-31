package org.example.service;

import org.example.data.DataStore;
import org.example.model.*;

import java.security.SecureRandom;
import java.util.*;

public class BookingService {
    private final DataStore db;
    private final PaymentService paymentService = new PaymentService();
    private final SecureRandom rnd = new SecureRandom();

    public BookingService(DataStore db) { this.db = db; }

    // 自動選位：挑最前面可用的座位
    public List<String> autoSelectSeats(String showtimeId, int qty) {
        Map<String, Boolean> seats = db.seatMap.get(showtimeId);
        if (seats == null) return List.of();

        List<String> picked = new ArrayList<>();
        for (var e : seats.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                picked.add(e.getKey());
                if (picked.size() == qty) break;
            }
        }
        return picked.size() == qty ? picked : List.of();
    }

    public List<String> listAvailableSeats(String showtimeId, int limit) {
        Map<String, Boolean> seats = db.seatMap.get(showtimeId);
        if (seats == null) return List.of();

        List<String> out = new ArrayList<>();
        for (var e : seats.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) out.add(e.getKey());
            if (out.size() == limit) break;
        }
        return out;
    }

    // 建立訂單：會把座位先鎖住（available=false）
    public Booking createBooking(String userId, String showtimeId, List<String> seatIds,
                                 TicketType ticketType, MealCombo combo, String discountCode) {
        Showtime st = db.showtimes.get(showtimeId);
        if (st == null) return null;

        // 檢查座位可用並鎖位
        Map<String, Boolean> seats = db.seatMap.get(showtimeId);
        if (seats == null) return null;

        // 先檢查全部 seatId 存在且可用
        for (String seatId : seatIds) {
            Boolean ok = seats.get(seatId);
            if (ok == null || !ok) return null;
        }
        // 鎖位
        for (String seatId : seatIds) seats.put(seatId, false);

        int base = st.basePrice();
        int ticketPrice = applyTicketType(base, ticketType);
        int mealPrice = mealPrice(combo);
        int subtotal = ticketPrice * seatIds.size() + mealPrice;

        Integer discount = null;
        if (discountCode != null) {
            Integer d = db.discountCodes.get(discountCode);
            if (d == null) {
                // 無效折扣碼：釋放座位
                for (String seatId : seatIds) seats.put(seatId, true);
                return null;
            }
            discount = d;
        }

        int total = Math.max(0, subtotal - (discount == null ? 0 : discount));
        String bookingId = "B" + System.currentTimeMillis() + (100 + rnd.nextInt(900));

        Booking b = new Booking(
                bookingId, userId, showtimeId, List.copyOf(seatIds),
                ticketType, combo, discount, total,
                BookingStatus.CREATED, System.currentTimeMillis()
        );

        db.bookings.put(bookingId, b);
        return b;
    }

    public PaymentResult pay(String bookingId, String cardNo, String exp, String cvv) {
        Booking b = db.bookings.get(bookingId);
        if (b == null) return new PaymentResult(false, "找不到訂單");
        if (b.status() != BookingStatus.CREATED) return new PaymentResult(false, "訂單狀態不可付款：" + b.status());

        PaymentResult pr = paymentService.validateAndPay(cardNo, exp, cvv, b.createdAtMillis());
        if (!pr.success()) return pr;

        // 標記已付款 + 生成電子票券 + 熱門統計
        Booking paid = new Booking(
                b.bookingId(), b.userId(), b.showtimeId(), b.seatIds(), b.ticketType(),
                b.mealCombo(), b.discountAmount(), b.totalPrice(),
                BookingStatus.PAID, b.createdAtMillis()
        );
        db.bookings.put(bookingId, paid);

        Showtime st = db.showtimes.get(b.showtimeId());
        db.popularity.put(st.movieId(), db.popularity.getOrDefault(st.movieId(), 0) + 1);

        generateTickets(paid);
        return new PaymentResult(true, "付款成功");
    }

    public void cancelUnpaid(String bookingId) {
        Booking b = db.bookings.get(bookingId);
        if (b == null) return;
        if (b.status() != BookingStatus.CREATED) return;

        // 釋放座位
        Map<String, Boolean> seats = db.seatMap.get(b.showtimeId());
        if (seats != null) {
            for (String seatId : b.seatIds()) seats.put(seatId, true);
        }

        Booking canceled = new Booking(
                b.bookingId(), b.userId(), b.showtimeId(), b.seatIds(), b.ticketType(),
                b.mealCombo(), b.discountAmount(), b.totalPrice(),
                BookingStatus.CANCELED, b.createdAtMillis()
        );
        db.bookings.put(bookingId, canceled);
    }

    public List<Booking> listBookingsByUser(String userId) {
        return db.bookings.values().stream()
                .filter(b -> b.userId().equals(userId))
                .sorted((a,b) -> Long.compare(b.createdAtMillis(), a.createdAtMillis()))
                .toList();
    }

    public List<Ticket> getTicketsByBooking(String bookingId) {
        return db.tickets.values().stream()
                .filter(t -> t.bookingId().equals(bookingId))
                .toList();
    }

    private void generateTickets(Booking b) {
        for (String seatId : b.seatIds()) {
            String ticketId = "TK" + System.nanoTime() + rnd.nextInt(1000);
            String code = "ETK-" + b.bookingId() + "-" + seatId + "-" + (100000 + rnd.nextInt(900000));
            db.tickets.put(ticketId, new Ticket(ticketId, b.bookingId(), seatId, code));
        }
    }

    private int applyTicketType(int base, TicketType t) {
        return switch (t) {
            case STUDENT -> (int)Math.round(base * 0.85);
            case EARLY_BIRD -> (int)Math.round(base * 0.80);
            default -> base;
        };
    }

    private int mealPrice(MealCombo c) {
        return switch (c) {
            case POPCORN_COLA -> 120;
            case COUPLE_SET -> 220;
            default -> 0;
        };
    }
}
