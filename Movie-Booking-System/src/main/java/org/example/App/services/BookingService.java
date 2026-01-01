package org.example.App.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 訂票核心服務（示範版）
 * - 電影清單
 * - 影城清單（用於推薦/顯示）
 * - 座位占用（以 movie+cinema+date+time 為一個場次 showKey）
 * - 訂單建立 / 付款確認 / 退票
 * - 餘票更新（以電影為單位簡化）
 */
public class BookingService {

    /** 座位配置：與 BookingModule 的座位格一致 */
    public static final int SEAT_ROWS = 8;
    public static final int SEAT_COLS = 12;
    public static final int SEAT_CAPACITY = SEAT_ROWS * SEAT_COLS; // 96

    private final List<Movie> movies = new ArrayList<>();
    private final List<Cinema> cinemas = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    /** 以電影為單位的剩餘（示範：不區分場次） */
    private final Map<String, Integer> movieRemaining = new HashMap<>();

    /** 已被訂走的座位（依 showKey 場次管理） */
    private final Map<String, Set<String>> bookedSeatsByShow = new HashMap<>();

    /** ✅ 定義 Movie 內部類 */
    public static class Movie {
        public String title;
        public String director;
        public String description;
        public double rating;
        public int duration;

        public Movie(String title, String director, String description, double rating, int duration) {
            this.title = title;
            this.director = director;
            this.description = description;
            this.rating = rating;
            this.duration = duration;
        }
    }

    /** ✅ 影城資訊（用於推薦/顯示） */
    public static class Cinema {
        public String name;
        public String area;     // 地區（台北/新北/桃園...）
        public String address;  // 地址（示範）

        public Cinema(String name, String area, String address) {
            this.name = name;
            this.area = area;
            this.address = address;
        }
    }

    /** ✅ 訂單（Booking） */
    public static class Booking {
        public String bookingId;
        public String userId;
        public String movieTitle;
        public String cinema;
        public LocalDate bookingDate;  // 此處作為「場次日期」
        public LocalTime bookingTime;  // 此處作為「場次時間」
        public List<String> seats;
        public int totalPrice;

        // 額外資訊
        public String ticketType;
        public String discountCode;
        public String meal;
        public String paymentMethod;
        public String idNumber; // 身分證字號（或學生驗證用）

        // 電子票券/狀態
        public String ticketCode; // 付款成功才產生
        public String status;     // 已確認 / 已付款 / 已退票
        public LocalDateTime createdAt;
        public LocalDateTime paidAt;
        public LocalDateTime refundedAt;

        public Booking(String bookingId, String userId, String movieTitle, String cinema,
                       LocalDate bookingDate, LocalTime bookingTime, List<String> seats,
                       int totalPrice, String ticketType, String discountCode, String meal,
                       String paymentMethod, String idNumber) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.movieTitle = movieTitle;
            this.cinema = cinema;
            this.bookingDate = bookingDate;
            this.bookingTime = bookingTime;
            this.seats = new ArrayList<>(seats);
            this.totalPrice = totalPrice;
            this.ticketType = ticketType;
            this.discountCode = discountCode;
            this.meal = meal;
            this.paymentMethod = paymentMethod;
            this.idNumber = idNumber;
            this.ticketCode = null;
            this.status = "已確認";
            this.createdAt = LocalDateTime.now();
        }

        private String generateTicketCode() {
            // 6 位數碼（示範），避免太長
            long n = Math.abs(System.currentTimeMillis()) % 1_000_000;
            return "TK" + String.format("%06d", n);
        }

        private void issueTicketIfNeeded() {
            if (this.ticketCode == null) {
                this.ticketCode = generateTicketCode();
            }
        }
    }

    public BookingService() {
        initializeMovies();
        initializeCinemas();
    }

    private void initializeMovies() {
        movies.add(new Movie(
                "阿凡達：火與燼",
                "詹姆斯·卡梅隆",
                "潘多拉星球的冒險繼續。傑克和納美人族必須聯合對抗新的威脅。",
                9.2,
                192
        ));
        movies.add(new Movie(
                "黑豹：瓦坎達萬歲",
                "瑞恩·庫格勒",
                "瓦坎達的英雄們為王國而戰，守護他們的家園和人民。",
                8.8,
                161
        ));
        movies.add(new Movie(
                "奧本海默",
                "克里斯托弗·諾蘭",
                "美國物理學家J·羅伯特·奧本海默和曼哈頓計畫的故事。",
                8.5,
                180
        ));
        movies.add(new Movie(
                "劇場版 咒術迴戰 0",
                "朴性厚",
                "在詛咒肆虐的世界裡，少年們踏上救贖之路。",
                8.9,
                150
        ));
        movies.add(new Movie(
                "鬼滅之刃 遊郭篇",
                "外崎春雄",
                "炭治郎進入燈紅酒綠的遊郭，對抗上弦妓夫太郎。",
                9.0,
                144
        ));

        // 以「座位格容量」當作每部片的示範餘票上限
        for (Movie movie : movies) {
            movieRemaining.put(movie.title, SEAT_CAPACITY);
        }
    }

    private void initializeCinemas() {
        cinemas.add(new Cinema("威秀電影城", "台北", "台北市信義區松壽路20號"));
        cinemas.add(new Cinema("信義威秀", "台北", "台北市信義區松壽路18號"));
        cinemas.add(new Cinema("國賓大戲院", "台北", "台北市中山區南京東路二段52號"));
        cinemas.add(new Cinema("美麗華影城", "台北", "台北市中山區敬業三路20號"));
        cinemas.add(new Cinema("板橋大遠百威秀", "新北", "新北市板橋區新站路28號"));
        cinemas.add(new Cinema("林口MITSUI威秀", "新北", "新北市林口區文化三路一段356號"));
        cinemas.add(new Cinema("桃園統領威秀", "桃園", "桃園市桃園區中正路61號"));
    }

    // =========================
    //  Query
    // =========================

    public List<Movie> getMovies() {
        return new ArrayList<>(movies);
    }

    public Movie getMovieByTitle(String title) {
        for (Movie movie : movies) {
            if (movie.title.equals(title)) return movie;
        }
        return null;
    }

    public List<Cinema> getCinemas() {
        return new ArrayList<>(cinemas);
    }

    public List<Cinema> getNearestCinemas(String area, int limit) {
        // 這裡示範用 area 字串做簡單匹配
        List<Cinema> list = new ArrayList<>();
        if (area != null && !area.isBlank()) {
            for (Cinema c : cinemas) {
                if (c.area.equalsIgnoreCase(area.trim())) list.add(c);
            }
        }
        if (list.isEmpty()) list.addAll(cinemas);
        return list.subList(0, Math.min(limit, list.size()));
    }

    /** 最熱門電影（依已付款訂單數排序） */
    public List<String> getMostPopularMovies(int limit) {
        Map<String, Integer> count = new HashMap<>();
        for (Booking b : bookings) {
            if (!"已付款".equals(b.status)) continue;
            count.put(b.movieTitle, count.getOrDefault(b.movieTitle, 0) + 1);
        }
        List<String> titles = new ArrayList<>(count.keySet());
        titles.sort((a, b) -> Integer.compare(count.getOrDefault(b, 0), count.getOrDefault(a, 0)));
        if (titles.isEmpty()) {
            // 沒有資料就用評分排序
            List<Movie> ms = getMovies();
            ms.sort((m1, m2) -> Double.compare(m2.rating, m1.rating));
            for (Movie m : ms) titles.add(m.title);
        }
        return titles.subList(0, Math.min(limit, titles.size()));
    }

    /** 場次 key（座位占用用） */
    public String buildShowKey(String movieTitle, String cinema, LocalDate date, LocalTime time) {
        return movieTitle + "|" + cinema + "|" + date + "|" + time;
    }

    /** 取得某場次已被訂走的座位 */
    public Set<String> getBookedSeats(String showKey) {
        Set<String> set = bookedSeatsByShow.getOrDefault(showKey, Collections.emptySet());
        return new HashSet<>(set);
    }

    public boolean isSeatAvailable(String showKey, String seatId) {
        return !bookedSeatsByShow.getOrDefault(showKey, Collections.emptySet()).contains(seatId);
    }

    // =========================
    //  Booking lifecycle
    // =========================

    /**
     * 建立訂單（已確認：尚未付款）
     * - 會先「占用座位」（避免同場次重複選位）
     */
    public Booking createBooking(String userId, String movieTitle, String cinema,
                                 LocalDate date, LocalTime time, List<String> seats, int totalPrice,
                                 String ticketType, String discountCode, String meal,
                                 String paymentMethod, String idNumber) {

        String showKey = buildShowKey(movieTitle, cinema, date, time);

        // 1) 座位檢查
        if (seats == null || seats.isEmpty()) return null;
        for (String seat : seats) {
            if (!isValidSeat(seat)) return null;
            if (!isSeatAvailable(showKey, seat)) return null;
        }

        // 2) 占用座位
        reserveSeats(showKey, seats);

        // 3) 建立訂單
        String bookingId = "BK" + (System.currentTimeMillis() % 1_000_000);
        Booking booking = new Booking(
                bookingId, userId, movieTitle, cinema, date, time,
                seats, totalPrice, ticketType, discountCode, meal, paymentMethod, idNumber
        );
        bookings.add(booking);
        return booking;
    }

    /** 向下相容：舊介面（不含額外資訊） */
    public Booking createBooking(String userId, String movieTitle, String cinema,
                                 LocalDate date, LocalTime time, List<String> seats, int totalPrice) {
        return createBooking(userId, movieTitle, cinema, date, time, seats, totalPrice,
                null, null, null, null, null);
    }

    /** 確認付款：生成電子票券 + 更新餘票 */
    public boolean confirmPayment(Booking booking) {
        if (booking == null) return false;
        if ("已退票".equals(booking.status)) return false;
        if ("已付款".equals(booking.status)) return true;

        booking.status = "已付款";
        booking.paidAt = LocalDateTime.now();
        booking.issueTicketIfNeeded();

        // 更新餘票（以電影為單位簡化）
        updateRemaining(booking.movieTitle, booking.seats != null ? booking.seats.size() : 0);
        return true;
    }

    /** 更新剩餘座位（以電影為單位） */
    public void updateRemaining(String movieTitle, int quantity) {
        int remaining = movieRemaining.getOrDefault(movieTitle, SEAT_CAPACITY);
        movieRemaining.put(movieTitle, Math.max(0, remaining - Math.max(0, quantity)));
    }

    /** 退票：
     * - 狀態改為已退票
     * - 釋放座位（同場次可再選）
     * - 餘票回補（示範版：加回電影餘票）
     */
    public boolean refundBooking(String bookingId) {
        for (Booking booking : bookings) {
            if (!booking.bookingId.equals(bookingId)) continue;

            if ("已退票".equals(booking.status)) return true;

            booking.status = "已退票";
            booking.refundedAt = LocalDateTime.now();

            String showKey = buildShowKey(booking.movieTitle, booking.cinema, booking.bookingDate, booking.bookingTime);
            releaseSeats(showKey, booking.seats);

            // 回補餘票（示範）
            int remaining = movieRemaining.getOrDefault(booking.movieTitle, SEAT_CAPACITY);
            int add = booking.seats != null ? booking.seats.size() : 0;
            movieRemaining.put(booking.movieTitle, Math.min(SEAT_CAPACITY, remaining + add));

            return true;
        }
        return false;
    }

    // =========================
    //  Orders query
    // =========================

    public List<Booking> getUserBookings(String userId) {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.userId.equals(userId)) userBookings.add(booking);
        }
        return userBookings;
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    public Booking getBookingById(String bookingId) {
        for (Booking b : bookings) {
            if (b.bookingId.equals(bookingId)) return b;
        }
        return null;
    }

    public int getRemaining(String movieTitle) {
        return movieRemaining.getOrDefault(movieTitle, SEAT_CAPACITY);
    }

    // =========================
    //  Seat helpers
    // =========================

    private boolean isValidSeat(String seatId) {
        if (seatId == null || seatId.length() < 2) return false;
        char row = Character.toUpperCase(seatId.charAt(0));
        if (row < 'A' || row >= ('A' + SEAT_ROWS)) return false;
        String numStr = seatId.substring(1);
        try {
            int col = Integer.parseInt(numStr);
            return col >= 1 && col <= SEAT_COLS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void reserveSeats(String showKey, List<String> seats) {
        Set<String> set = bookedSeatsByShow.computeIfAbsent(showKey, k -> new HashSet<>());
        set.addAll(seats);
    }

    private void releaseSeats(String showKey, List<String> seats) {
        if (seats == null || seats.isEmpty()) return;
        Set<String> set = bookedSeatsByShow.get(showKey);
        if (set == null) return;
        set.removeAll(seats);
    }
}
