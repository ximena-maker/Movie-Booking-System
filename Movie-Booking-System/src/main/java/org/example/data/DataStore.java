package org.example.data;

import org.example.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class DataStore {
    public final Map<String, User> users = new HashMap<>();
    public final Map<String, Movie> movies = new HashMap<>();
    public final Map<String, Theater> theaters = new HashMap<>();
    public final Map<String, Showtime> showtimes = new HashMap<>();

    // showtimeId -> seatId -> available
    public final Map<String, Map<String, Boolean>> seatMap = new HashMap<>();

    public final Map<String, Booking> bookings = new HashMap<>();
    public final Map<String, Ticket> tickets = new HashMap<>();

    // 折扣碼
    public final Map<String, Integer> discountCodes = new HashMap<>();

    // 熱門統計（movieId -> paid count）
    public final Map<String, Integer> popularity = new HashMap<>();

    public static DataStore bootstrapSample() {
        DataStore db = new DataStore();

        // users（示範用：密碼直接做 hash 不是重點，先用簡單 hash）
        db.users.put("u1", new User("u1", "alice", Integer.toString("1234".hashCode())));
        db.users.put("u2", new User("u2", "bob", Integer.toString("abcd".hashCode())));

        // movies
        db.movies.put("M1", new Movie("M1", "Interstellar", "PG-13"));
        db.movies.put("M2", new Movie("M2", "Spirited Away", "G"));
        db.movies.put("M3", new Movie("M3", "Avengers", "PG-13"));

        // theaters（用 x,y 當座標）
        db.theaters.put("T1", new Theater("T1", "台北信義影城", 10, 10));
        db.theaters.put("T2", new Theater("T2", "新北板橋影城", 20, 8));
        db.theaters.put("T3", new Theater("T3", "桃園中壢影城", 35, 12));

        // showtimes
        db.showtimes.put("S1", new Showtime("S1", "T1", "M1", LocalDateTime.now().plusHours(3), 350));
        db.showtimes.put("S2", new Showtime("S2", "T2", "M1", LocalDateTime.now().plusHours(4), 320));
        db.showtimes.put("S3", new Showtime("S3", "T1", "M2", LocalDateTime.now().plusHours(2), 300));
        db.showtimes.put("S4", new Showtime("S4", "T3", "M3", LocalDateTime.now().plusHours(5), 360));

        // seat maps（簡化：A1~A10, B1~B10）
        for (Showtime st : db.showtimes.values()) {
            Map<String, Boolean> seats = new LinkedHashMap<>();
            for (char row : new char[]{'A', 'B'}) {
                for (int i = 1; i <= 10; i++) {
                    seats.put(row + Integer.toString(i), true);
                }
            }
            db.seatMap.put(st.showtimeId(), seats);
        }

        // discount codes
        db.discountCodes.put("OFF50", 50);
        db.discountCodes.put("OFF100", 100);

        // popularity init
        db.movies.keySet().forEach(mid -> db.popularity.put(mid, 0));

        return db;
    }
}
