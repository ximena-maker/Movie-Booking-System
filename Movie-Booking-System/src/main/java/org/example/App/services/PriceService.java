package org.example.App.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 價格/優惠服務（示範版）
 * - 比價：回傳各影城對應票價
 * - 優惠：依情境判斷可用折扣（早鳥/學生/團體/會員）
 */
public class PriceService {

    /** 影城格式 + 票種 的報價 */
    public static class PriceQuote {
        public String cinema;
        public String format;     // 2D / 3D / IMAX
        public String ticketType; // ADULT / STUDENT / SENIOR / CHILD
        public int price;
        public String source;

        public PriceQuote(String cinema, String format, String ticketType, int price, String source) {
            this.cinema = cinema;
            this.format = format;
            this.ticketType = ticketType;
            this.price = price;
            this.source = source;
        }
    }

    /** 優惠資料 */
    public static class Discount {
        public String code;
        public String name;
        public int percentage; // 例如 20 = 20% off
        public String type;    // EARLY_BIRD / STUDENT / GROUP / MEMBER

        public Discount(String code, String name, int percentage, String type) {
            this.code = code;
            this.name = name;
            this.percentage = percentage;
            this.type = type;
        }
    }

    /** 折扣判斷所需情境 */
    public static class DiscountContext {
        public LocalDate showDate;
        public int quantity;
        public boolean isMember;
        public boolean isStudentVerified;

        public DiscountContext(LocalDate showDate, int quantity, boolean isMember, boolean isStudentVerified) {
            this.showDate = showDate;
            this.quantity = quantity;
            this.isMember = isMember;
            this.isStudentVerified = isStudentVerified;
        }
    }

    private final Map<String, Map<String, Integer>> priceMap = new LinkedHashMap<>();
    private final List<Discount> discounts = new ArrayList<>();

    public PriceService() {
        initPrices();
        initDiscounts();
    }

    private void initPrices() {
        // Key = format + "-" + ticketType
        // ticketType: ADULT / STUDENT / SENIOR / CHILD

        Map<String, Integer> vieshow = new HashMap<>();
        vieshow.put("2D-ADULT", 320);
        vieshow.put("2D-STUDENT", 280);
        vieshow.put("2D-SENIOR", 250);
        vieshow.put("2D-CHILD", 240);
        vieshow.put("3D-ADULT", 350);
        vieshow.put("3D-STUDENT", 320);
        vieshow.put("3D-SENIOR", 300);
        vieshow.put("3D-CHILD", 290);
        vieshow.put("IMAX-ADULT", 380);
        vieshow.put("IMAX-STUDENT", 350);
        vieshow.put("IMAX-SENIOR", 330);
        vieshow.put("IMAX-CHILD", 320);
        priceMap.put("威秀電影城", vieshow);

        // 信義威秀：此處示範略高 10 元
        Map<String, Integer> xinyi = new HashMap<>();
        for (Map.Entry<String, Integer> e : vieshow.entrySet()) {
            xinyi.put(e.getKey(), e.getValue() + 10);
        }
        priceMap.put("信義威秀", xinyi);

        Map<String, Integer> ambassador = new HashMap<>();
        ambassador.put("2D-ADULT", 340);
        ambassador.put("2D-STUDENT", 300);
        ambassador.put("2D-SENIOR", 270);
        ambassador.put("2D-CHILD", 260);
        ambassador.put("3D-ADULT", 360);
        ambassador.put("3D-STUDENT", 330);
        ambassador.put("3D-SENIOR", 310);
        ambassador.put("3D-CHILD", 300);
        ambassador.put("IMAX-ADULT", 390);
        ambassador.put("IMAX-STUDENT", 360);
        ambassador.put("IMAX-SENIOR", 340);
        ambassador.put("IMAX-CHILD", 330);
        priceMap.put("國賓大戲院", ambassador);

        Map<String, Integer> miramar = new HashMap<>();
        miramar.put("2D-ADULT", 330);
        miramar.put("2D-STUDENT", 290);
        miramar.put("2D-SENIOR", 260);
        miramar.put("2D-CHILD", 250);
        miramar.put("3D-ADULT", 355);
        miramar.put("3D-STUDENT", 325);
        miramar.put("3D-SENIOR", 305);
        miramar.put("3D-CHILD", 295);
        miramar.put("IMAX-ADULT", 385);
        miramar.put("IMAX-STUDENT", 355);
        miramar.put("IMAX-SENIOR", 335);
        miramar.put("IMAX-CHILD", 325);
        priceMap.put("美麗華影城", miramar);
    }

    private void initDiscounts() {
        discounts.clear();
        discounts.add(new Discount("EARLY20", "早鳥票（7天前購票）", 20, "EARLY_BIRD"));
        discounts.add(new Discount("STUDENT15", "學生折扣（需身份驗證）", 15, "STUDENT"));
        discounts.add(new Discount("GROUP10", "團體票（10張以上）", 10, "GROUP"));
        discounts.add(new Discount("MEMBER5", "會員折扣", 5, "MEMBER"));
    }

    /** 取得所有影城（顯示用） */
    public List<String> getCinemas() {
        return new ArrayList<>(priceMap.keySet());
    }

    /**
     * 比價：format=2D/3D/IMAX；ticketType=ADULT/STUDENT/SENIOR/CHILD
     */
    public List<PriceQuote> compare(String format, String ticketType) {
        String key = format + "-" + ticketType;
        List<PriceQuote> quotes = new ArrayList<>();

        for (String cinema : priceMap.keySet()) {
            Map<String, Integer> prices = priceMap.get(cinema);
            if (prices != null && prices.containsKey(key)) {
                int price = prices.get(key);
                quotes.add(new PriceQuote(cinema, format, ticketType, price, cinema + "（示範價）"));
            }
        }

        quotes.sort(Comparator.comparingInt(q -> q.price));
        return quotes;
    }

    public int getLowestPrice(String format, String ticketType) {
        List<PriceQuote> quotes = compare(format, ticketType);
        return quotes.isEmpty() ? -1 : quotes.get(0).price;
    }

    public Integer getPrice(String cinema, String format, String ticketType) {
        Map<String, Integer> prices = priceMap.get(cinema);
        if (prices == null) return null;
        return prices.get(format + "-" + ticketType);
    }

    /** 取得全部折扣（不代表可用） */
    public List<Discount> getAllDiscounts() {
        return new ArrayList<>(discounts);
    }

    /** 取得可用折扣（依情境） */
    public List<Discount> getApplicableDiscounts(DiscountContext ctx) {
        List<Discount> out = new ArrayList<>();
        for (Discount d : discounts) {
            if (isApplicable(d, ctx)) out.add(d);
        }
        return out;
    }

    /** 是否可用 */
    public boolean isDiscountApplicable(String discountCode, DiscountContext ctx) {
        if (discountCode == null || discountCode.isBlank()) return false;
        Discount d = findDiscount(discountCode);
        return d != null && isApplicable(d, ctx);
    }

    /** 套用折扣（若不符合情境，回傳原價） */
    public int applyDiscount(int price, String discountCode, DiscountContext ctx) {
        Discount d = findDiscount(discountCode);
        if (d == null) return price;
        if (!isApplicable(d, ctx)) return price;
        double factor = (100.0 - d.percentage) / 100.0;
        return (int) Math.round(price * factor);
    }

    private Discount findDiscount(String code) {
        for (Discount d : discounts) {
            if (d.code.equalsIgnoreCase(code)) return d;
        }
        return null;
    }

    private boolean isApplicable(Discount d, DiscountContext ctx) {
        if (ctx == null) return false;
        switch (d.type) {
            case "EARLY_BIRD": {
                if (ctx.showDate == null) return false;
                long days = ChronoUnit.DAYS.between(LocalDate.now(), ctx.showDate);
                return days >= 7;
            }
            case "STUDENT":
                return ctx.isStudentVerified;
            case "GROUP":
                return ctx.quantity >= 10;
            case "MEMBER":
                return ctx.isMember;
            default:
                return false;
        }
    }
}
