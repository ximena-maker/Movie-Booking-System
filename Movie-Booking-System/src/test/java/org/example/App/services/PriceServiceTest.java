package org.example.App.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceServiceTest {

  private PriceService priceService;

  @BeforeEach
  void setup() {
    priceService = new PriceService();
  }

  // =========================
  // Cinema / price query
  // =========================

  @Test
  void getCinemas_shouldNotBeEmpty() {
    List<String> cinemas = priceService.getCinemas();
    assertFalse(cinemas.isEmpty());
  }

  @Test
  void getPrice_existingCinema_shouldReturnPrice() {
    Integer price = priceService.getPrice("威秀電影城", "2D", "ADULT");
    assertNotNull(price);
    assertTrue(price > 0);
  }

  @Test
  void getPrice_nonExistingCinema_shouldReturnNull() {
    Integer price = priceService.getPrice("不存在影城", "2D", "ADULT");
    assertNull(price);
  }

  @Test
  void getLowestPrice_existingFormat_shouldReturnPositive() {
    int price = priceService.getLowestPrice("2D", "ADULT");
    assertTrue(price > 0);
  }

  @Test
  void getLowestPrice_invalidFormat_shouldReturnMinusOne() {
    int price = priceService.getLowestPrice("4DX", "ADULT");
    assertEquals(-1, price);
  }

  // =========================
  // Compare
  // =========================

  @Test
  void compare_shouldReturnSortedByPrice() {
    List<PriceService.PriceQuote> quotes =
        priceService.compare("2D", "ADULT");

    assertFalse(quotes.isEmpty());

    for (int i = 1; i < quotes.size(); i++) {
      assertTrue(quotes.get(i).price >= quotes.get(i - 1).price);
    }
  }

  // =========================
  // Discounts basic
  // =========================

  @Test
  void getAllDiscounts_shouldContainEarlyBird() {
    boolean found = priceService.getAllDiscounts().stream()
        .anyMatch(d -> d.code.equals("EARLY20"));
    assertTrue(found);
  }

  // =========================
  // Discount applicability
  // =========================

  @Test
  void earlyBirdDiscount_shouldBeApplicable_when7DaysBefore() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(7),
            1,
            false,
            false
        );

    assertTrue(priceService.isDiscountApplicable("EARLY20", ctx));
  }

  @Test
  void earlyBirdDiscount_shouldNotBeApplicable_whenLessThan7Days() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(3),
            1,
            false,
            false
        );

    assertFalse(priceService.isDiscountApplicable("EARLY20", ctx));
  }

  @Test
  void studentDiscount_shouldRequireVerification() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10),
            1,
            false,
            true
        );

    assertTrue(priceService.isDiscountApplicable("STUDENT15", ctx));
  }

  @Test
  void studentDiscount_withoutVerification_shouldFail() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10),
            1,
            false,
            false
        );

    assertFalse(priceService.isDiscountApplicable("STUDENT15", ctx));
  }

  @Test
  void groupDiscount_shouldApply_whenQuantityAtLeast10() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(),
            10,
            false,
            false
        );

    assertTrue(priceService.isDiscountApplicable("GROUP10", ctx));
  }

  @Test
  void groupDiscount_shouldNotApply_whenQuantityLessThan10() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(),
            5,
            false,
            false
        );

    assertFalse(priceService.isDiscountApplicable("GROUP10", ctx));
  }

  @Test
  void memberDiscount_shouldApply_whenMember() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(),
            1,
            true,
            false
        );

    assertTrue(priceService.isDiscountApplicable("MEMBER5", ctx));
  }

  @Test
  void memberDiscount_shouldFail_whenNotMember() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(),
            1,
            false,
            false
        );

    assertFalse(priceService.isDiscountApplicable("MEMBER5", ctx));
  }

  @Test
  void isDiscountApplicable_nullCode_shouldReturnFalse() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(),
            1,
            true,
            true
        );

    assertFalse(priceService.isDiscountApplicable(null, ctx));
  }

  // =========================
  // Apply discount
  // =========================

  @Test
  void applyDiscount_validEarlyBird_shouldReducePrice() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10),
            1,
            false,
            false
        );

    int discounted = priceService.applyDiscount(300, "EARLY20", ctx);
    assertEquals(240, discounted);
  }

  @Test
  void applyDiscount_notApplicable_shouldReturnOriginalPrice() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(),
            1,
            false,
            false
        );

    int price = priceService.applyDiscount(300, "EARLY20", ctx);
    assertEquals(300, price);
  }

  @Test
  void applyDiscount_invalidCode_shouldReturnOriginalPrice() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10),
            1,
            true,
            true
        );

    int price = priceService.applyDiscount(300, "NOT_EXIST", ctx);
    assertEquals(300, price);
  }

  @Test
  void applyDiscount_nullContext_shouldReturnOriginalPrice() {
    int price = priceService.applyDiscount(300, "EARLY20", null);
    assertEquals(300, price);
  }

  @Test
  void getApplicableDiscounts_shouldReturnApplicableOnes() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10), // EARLY_BIRD
            10,                           // GROUP
            true,                         // MEMBER
            true                          // STUDENT
        );

    List<PriceService.Discount> list =
        priceService.getApplicableDiscounts(ctx);

    assertFalse(list.isEmpty());

    // 至少包含其中幾個
    assertTrue(list.stream().anyMatch(d -> d.code.equals("EARLY20")));
    assertTrue(list.stream().anyMatch(d -> d.code.equals("GROUP10")));
    assertTrue(list.stream().anyMatch(d -> d.code.equals("MEMBER5")));
  }

  @Test
  void getApplicableDiscounts_whenNoneApplicable_shouldReturnEmpty() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now(), // 非早鳥
            1,               // 非團體
            false,           // 非會員
            false            // 非學生
        );

    List<PriceService.Discount> list =
        priceService.getApplicableDiscounts(ctx);

    assertTrue(list.isEmpty());
  }


  @Test
  void getApplicableDiscounts_withNullContext_shouldReturnEmpty() {
    List<PriceService.Discount> list =
        priceService.getApplicableDiscounts(null);

    assertTrue(list.isEmpty());
  }

  @Test
  void compare_unknownFormat_shouldReturnEmpty() {
    List<PriceService.PriceQuote> quotes =
        priceService.compare("4DX", "ADULT");

    assertTrue(quotes.isEmpty());
  }

  @Test
  void getLowestPrice_noResult_shouldReturnMinusOne() {
    int price = priceService.getLowestPrice("4DX", "ADULT");
    assertEquals(-1, price);
  }

  @Test
  void getPrice_unknownCinema_shouldReturnNull() {
    Integer price = priceService.getPrice("不存在影城", "2D", "ADULT");
    assertNull(price);
  }



  @Test
  void isDiscountApplicable_blankCode_shouldReturnFalse() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10), 1, false, false
        );

    assertFalse(priceService.isDiscountApplicable("   ", ctx));
  }

  @Test
  void applyDiscount_unknownCode_shouldReturnOriginalPrice() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10), 1, false, false
        );

    int price = priceService.applyDiscount(300, "UNKNOWN", ctx);
    assertEquals(300, price);
  }

  @Test
  void getApplicableDiscounts_nullContext_shouldReturnEmpty() {
    List<PriceService.Discount> discounts =
        priceService.getApplicableDiscounts(null);

    assertTrue(discounts.isEmpty());
  }

  @Test
  void earlyBirdDiscount_nullShowDate_shouldNotApply() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            null, 1, false, false
        );

    assertFalse(priceService.isDiscountApplicable("EARLY20", ctx));
  }

  @Test
  void isApplicable_unknownType_shouldReturnFalse() {
    PriceService.DiscountContext ctx =
        new PriceService.DiscountContext(
            LocalDate.now().plusDays(10), 1, false, false
        );

    PriceService.Discount d =
        priceService.getAllDiscounts().get(0);
    d.type = "UNKNOWN";

    assertFalse(priceService.isDiscountApplicable(d.code, ctx));
  }


}
