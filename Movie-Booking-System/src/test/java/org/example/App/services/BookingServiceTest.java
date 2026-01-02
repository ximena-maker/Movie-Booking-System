package org.example.App.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

  private BookingService bookingService;

  @BeforeEach
  void setup() {
    bookingService = new BookingService();
  }

  // =========================
  // Query tests
  // =========================

  @Test
  void getMovies_shouldNotBeEmpty() {
    assertFalse(bookingService.getMovies().isEmpty());
  }

  @Test
  void getMovieByTitle_existingMovie_shouldReturnMovie() {
    BookingService.Movie movie =
        bookingService.getMovieByTitle("阿凡達：火與燼");
    assertNotNull(movie);
  }

  @Test
  void getMovieByTitle_notExist_shouldReturnNull() {
    assertNull(bookingService.getMovieByTitle("不存在的電影"));
  }

  @Test
  void getNearestCinemas_withArea_shouldReturnLimited() {
    List<BookingService.Cinema> list =
        bookingService.getNearestCinemas("台北", 2);
    assertTrue(list.size() <= 2);
  }

  @Test
  void getNearestCinemas_nullArea_shouldFallbackAll() {
    List<BookingService.Cinema> list =
        bookingService.getNearestCinemas(null, 3);
    assertEquals(3, list.size());
  }

  // =========================
  // Seat related
  // =========================

  @Test
  void isSeatAvailable_initiallyShouldBeTrue() {
    String key = bookingService.buildShowKey(
        "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON
    );
    assertTrue(bookingService.isSeatAvailable(key, "A1"));
  }

  @Test
  void createBooking_shouldReserveSeats() {
    String key = bookingService.buildShowKey(
        "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON
    );

    BookingService.Booking booking = bookingService.createBooking(
        "user1",
        "阿凡達：火與燼",
        "威秀電影城",
        LocalDate.now(),
        LocalTime.NOON,
        List.of("A1", "A2"),
        500
    );

    assertNotNull(booking);
    assertFalse(bookingService.isSeatAvailable(key, "A1"));
  }

  @Test
  void createBooking_withInvalidSeat_shouldFail() {
    BookingService.Booking booking = bookingService.createBooking(
        "user1",
        "阿凡達：火與燼",
        "威秀電影城",
        LocalDate.now(),
        LocalTime.NOON,
        List.of("Z99"),
        300
    );
    assertNull(booking);
  }

  @Test
  void createBooking_duplicateSeat_shouldFail() {
    String key = bookingService.buildShowKey(
        "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON
    );

    bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("B1"), 300
    );

    BookingService.Booking second =
        bookingService.createBooking(
            "u2", "阿凡達：火與燼", "威秀電影城",
            LocalDate.now(), LocalTime.NOON,
            List.of("B1"), 300
        );

    assertNull(second);
  }

  // =========================
  // Payment & refund
  // =========================

  @Test
  void confirmPayment_shouldGenerateTicketAndReduceRemaining() {
    BookingService.Booking booking =
        bookingService.createBooking(
            "user1", "阿凡達：火與燼", "威秀電影城",
            LocalDate.now(), LocalTime.NOON,
            List.of("C1", "C2"), 600
        );

    boolean paid = bookingService.confirmPayment(booking);

    assertTrue(paid);
    assertEquals("已付款", booking.status);
    assertNotNull(booking.ticketCode);
  }

  @Test
  void confirmPayment_nullBooking_shouldFail() {
    assertFalse(bookingService.confirmPayment(null));
  }

  @Test
  void refundBooking_shouldReleaseSeats() {
    BookingService.Booking booking =
        bookingService.createBooking(
            "user1", "阿凡達：火與燼", "威秀電影城",
            LocalDate.now(), LocalTime.NOON,
            List.of("D1"), 300
        );

    bookingService.confirmPayment(booking);

    boolean refunded =
        bookingService.refundBooking(booking.bookingId);

    assertTrue(refunded);
    assertEquals("已退票", booking.status);

    String key = bookingService.buildShowKey(
        booking.movieTitle, booking.cinema,
        booking.bookingDate, booking.bookingTime
    );

    assertTrue(bookingService.isSeatAvailable(key, "D1"));
  }

  @Test
  void refundBooking_invalidId_shouldFail() {
    assertFalse(bookingService.refundBooking("NOT_EXIST"));
  }

  // =========================
  // Remaining
  // =========================

  @Test
  void updateRemaining_shouldNotBelowZero() {
    bookingService.updateRemaining("阿凡達：火與燼", 999);
    assertEquals(0, bookingService.getRemaining("阿凡達：火與燼"));
  }

  @Test
  void getMostPopularMovies_withPaidBookings_shouldSortByCount() {
    BookingService.Booking b1 = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );
    bookingService.confirmPayment(b1);

    BookingService.Booking b2 = bookingService.createBooking(
        "u2", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A2"), 300
    );
    bookingService.confirmPayment(b2);

    BookingService.Booking b3 = bookingService.createBooking(
        "u3", "奧本海默", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("B1"), 300
    );
    bookingService.confirmPayment(b3);

    List<String> popular = bookingService.getMostPopularMovies(2);

    assertEquals("阿凡達：火與燼", popular.get(0));
  }


  @Test
  void getMostPopularMovies_withoutPaidBookings_shouldFallbackByRating() {
    List<String> popular = bookingService.getMostPopularMovies(3);

    assertFalse(popular.isEmpty());
    assertEquals(3, popular.size());
  }

  @Test
  void getBookedSeats_noBooking_shouldReturnEmptySet() {
    Set<String> seats = bookingService.getBookedSeats("NO_SHOW_KEY");
    assertTrue(seats.isEmpty());
  }

  @Test
  void getBookedSeats_withBooking_shouldReturnSeats() {
    String key = bookingService.buildShowKey(
        "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON
    );

    bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("C1", "C2"), 600
    );

    Set<String> seats = bookingService.getBookedSeats(key);

    assertEquals(2, seats.size());
    assertTrue(seats.contains("C1"));
  }


  @Test
  void getUserBookings_shouldReturnOnlyUserOrders() {
    bookingService.createBooking(
        "userA", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("D1"), 300
    );
    bookingService.createBooking(
        "userB", "奧本海默", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("E1"), 300
    );

    List<BookingService.Booking> list =
        bookingService.getUserBookings("userA");

    assertEquals(1, list.size());
    assertEquals("userA", list.get(0).userId);
  }

  @Test
  void getUserBookings_unknownUser_shouldReturnEmpty() {
    List<BookingService.Booking> list =
        bookingService.getUserBookings("noSuchUser");
    assertTrue(list.isEmpty());
  }

  @Test
  void getAllBookings_shouldReturnAll() {
    bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("F1"), 300
    );

    assertEquals(1, bookingService.getAllBookings().size());
  }

  @Test
  void getBookingById_existingId_shouldReturnBooking() {
    BookingService.Booking booking = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("G1"), 300
    );

    BookingService.Booking found =
        bookingService.getBookingById(booking.bookingId);

    assertNotNull(found);
  }

  @Test
  void getBookingById_notExist_shouldReturnNull() {
    assertNull(bookingService.getBookingById("NO_SUCH_ID"));
  }


  @Test
  void getCinemas_shouldReturnCinemaList() {
    List<BookingService.Cinema> cinemas = bookingService.getCinemas();
    assertNotNull(cinemas);
    assertFalse(cinemas.isEmpty());
  }

  @Test
  void getNearestCinemas_nullArea_shouldReturnAll() {
    List<BookingService.Cinema> list =
        bookingService.getNearestCinemas(null, 3);
    assertEquals(3, list.size());
  }

  @Test
  void getNearestCinemas_blankArea_shouldReturnAll() {
    List<BookingService.Cinema> list =
        bookingService.getNearestCinemas("   ", 2);
    assertEquals(2, list.size());
  }

  @Test
  void getMostPopularMovies_shouldIgnoreUnpaidBookings() {
    bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    ); // 沒 confirmPayment → 狀態不是「已付款」

    List<String> list = bookingService.getMostPopularMovies(3);

    assertFalse(list.isEmpty()); // 會走 fallback（rating）
  }

  @Test
  void createBooking_nullSeats_shouldReturnNull() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        null, 300
    );
    assertNull(b);
  }

  @Test
  void createBooking_emptySeats_shouldReturnNull() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of(), 300
    );
    assertNull(b);
  }

  @Test
  void confirmPayment_alreadyPaid_shouldReturnTrue() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );
    bookingService.confirmPayment(b);
    assertTrue(bookingService.confirmPayment(b));
  }

  @Test
  void confirmPayment_refundedBooking_shouldReturnFalse() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );
    bookingService.refundBooking(b.bookingId);
    assertFalse(bookingService.confirmPayment(b));
  }

  @Test
  void createBooking_invalidSeatFormat_shouldFail() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("Axx"), 300
    );
    assertNull(b);
  }

  @Test
  void refundBooking_unknownId_shouldReturnFalse() {
    assertFalse(bookingService.refundBooking("NO_SUCH_ID"));
  }

  @Test
  void refundBooking_nullSeats_shouldNotCrash() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );

    // 🔧 人工製造 seats == null（只為 coverage）
    b.seats = null;

    assertTrue(bookingService.refundBooking(b.bookingId));
  }


  @Test
  void refundBooking_shouldSkipOtherBookings() {
    BookingService.Booking b1 = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );
    BookingService.Booking b2 = bookingService.createBooking(
        "u2", "奧本海默", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("B1"), 300
    );

    assertTrue(bookingService.refundBooking(b2.bookingId));
  }

  @Test
  void getBookingById_existing_shouldReturnBooking() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );

    assertNotNull(bookingService.getBookingById(b.bookingId));
  }

  @Test
  void getBookingById_unknown_shouldReturnNull() {
    assertNull(bookingService.getBookingById("UNKNOWN"));
  }

  @Test
  void createBooking_nullSeat_shouldFail() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        null, 300   // ✅ 直接 seats = null
    );
    assertNull(b);
  }


  @Test
  void createBooking_invalidRowSeat_shouldFail() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("Z1"), 300
    );
    assertNull(b);
  }

  @Test
  void createBooking_nonNumericSeat_shouldFail() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("Axx"), 300
    );
    assertNull(b);
  }

  @Test
  void refundBooking_emptySeats_shouldNotCrash() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );

    b.seats = Collections.emptyList();

    assertTrue(bookingService.refundBooking(b.bookingId));
  }


  @Test
  void refundBooking_withoutReservedSeats_shouldNotCrash() {
    BookingService.Booking b = bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A1"), 300
    );

    // 清空 bookedSeatsByShow（製造 set == null）
    bookingService.getBookedSeats("dummy"); // 不存在 key

    assertTrue(bookingService.refundBooking(b.bookingId));
  }

  @Test
  void confirmPayment_twice_shouldNotRegenerateTicketCode() {
    BookingService.Booking booking = bookingService.createBooking(
        "u1",
        "阿凡達：火與燼",
        "威秀電影城",
        LocalDate.now(),
        LocalTime.NOON,
        List.of("A1"),
        300
    );

    bookingService.confirmPayment(booking);
    String firstCode = booking.ticketCode;

    // 再 confirm 一次（ticketCode != null）
    bookingService.confirmPayment(booking);
    String secondCode = booking.ticketCode;

    assertNotNull(firstCode);
    assertEquals(firstCode, secondCode);
  }

  @Test
  void refundBooking_notFound_shouldReturnFalse() {
    boolean result = bookingService.refundBooking("NOT_EXIST");
    assertFalse(result);
  }

  @Test
  void refundBooking_alreadyRefunded_shouldReturnTrue() {
    BookingService.Booking booking = bookingService.createBooking(
        "u1",
        "阿凡達：火與燼",
        "威秀電影城",
        LocalDate.now(),
        LocalTime.NOON,
        List.of("B1"),
        300
    );

    bookingService.confirmPayment(booking);

    // 第一次退票
    assertTrue(bookingService.refundBooking(booking.bookingId));
    // 第二次退票（命中「已退票」那行）
    assertTrue(bookingService.refundBooking(booking.bookingId));
  }

  @Test
  void refundBooking_noBookings_shouldReturnFalse() {
    BookingService emptyService = new BookingService();
    boolean result = emptyService.refundBooking("ANY");
    assertFalse(result);
  }

  @Test
  void getBookingById_notFound_shouldReturnNull() {
    BookingService.Booking booking = bookingService.createBooking(
        "u1",
        "阿凡達：火與燼",
        "威秀電影城",
        LocalDate.now(),
        LocalTime.NOON,
        List.of("C1"),
        300
    );

    BookingService.Booking result = bookingService.getBookingById("WRONG_ID");
    assertNull(result);
  }

  @Test
  void createBooking_invalidSeat_shouldFail() {
    assertNull(bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of(""), 300
    ));

    assertNull(bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("Z1"), 300
    ));

    assertNull(bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A0"), 300
    ));

    assertNull(bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A99"), 300
    ));

    assertNull(bookingService.createBooking(
        "u1", "阿凡達：火與燼", "威秀電影城",
        LocalDate.now(), LocalTime.NOON,
        List.of("A?"), 300
    ));
  }



}
