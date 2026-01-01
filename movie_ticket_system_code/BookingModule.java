package org.example.App.modules;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.example.App.services.BookingService;
import org.example.App.services.PriceService;
import org.example.App.services.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BookingModule {

    private final BookingService bookingService;
    private final PriceService priceService;
    private final UserService userService;

    private final List<String> selectedSeats = new ArrayList<>();

    public BookingModule(BookingService bookingService, PriceService priceService, UserService userService) {
        this.bookingService = bookingService;
        this.priceService = priceService;
        this.userService = userService;
    }

    public Node build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("🎟️ 電影訂票系統");
        title.setStyle("-fx-font-size: 28; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox bookingBox = createBookingForm();
        root.getChildren().addAll(title, bookingBox);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    private VBox createBookingForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 15; " +
                "-fx-background-color: rgba(26,38,55,0.8);");

        Label formTitle = new Label("📌 訂票資訊");
        formTitle.setStyle("-fx-font-size: 18; -fx-text-fill: #32b8c6; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        ComboBox<String> movieSelector = createStyledComboBox();
        for (BookingService.Movie movie : bookingService.getMovies()) {
            movieSelector.getItems().add(movie.title);
        }
        movieSelector.setValue(movieSelector.getItems().isEmpty() ? null : movieSelector.getItems().get(0));

        ComboBox<String> cinemaSelector = createStyledComboBox();
        cinemaSelector.getItems().addAll(priceService.getCinemas());
        cinemaSelector.setValue(cinemaSelector.getItems().isEmpty() ? null : cinemaSelector.getItems().get(0));

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setStyle("-fx-font-size: 14; -fx-padding: 5;");

        ComboBox<String> timeSelector = createStyledComboBox();
        timeSelector.getItems().addAll("10:00", "13:00", "16:00", "19:00", "22:00");
        timeSelector.setValue("19:00");

        ComboBox<String> formatSelector = createStyledComboBox();
        formatSelector.getItems().addAll("2D", "3D", "IMAX");
        formatSelector.setValue("2D");

        ComboBox<String> ticketTypeSelector = createStyledComboBox();
        ticketTypeSelector.getItems().addAll("全票", "學生票", "敬老票", "孩童票");
        ticketTypeSelector.setValue("全票");

        Spinner<Integer> ticketQuantity = new Spinner<>(1, 10, 1);
        ticketQuantity.setEditable(true);
        ticketQuantity.setStyle("-fx-font-size: 14;");

        // 身分證驗證（簡化：輸入字號 + 驗證）
        TextField idField = new TextField();
        idField.setPromptText("身分證字號 (例: A123456789)");
        idField.setStyle("-fx-font-size: 14; -fx-padding: 8;");

        CheckBox idVerified = new CheckBox("已完成身分證驗證");
        idVerified.setStyle("-fx-text-fill: white;");

        Label remainingLabel = new Label();
        remainingLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.75);");

        // 票價顯示
        Label priceLabel = new Label("票價：—");
        priceLabel.setStyle("-fx-text-fill: #32b8c6; -fx-font-size: 14; -fx-font-weight: bold;");

        // 折扣選擇
        ComboBox<String> discountSelector = createStyledComboBox();
        discountSelector.getItems().add("不使用折扣");
        discountSelector.setValue("不使用折扣");

        // 配餐
        ComboBox<String> mealSelector = createStyledComboBox();
        mealSelector.getItems().addAll(
                "不加購", "爆米花 + 可樂套餐", "熱狗堡 + 可樂套餐", "雞塊 + 可樂套餐", "雙人分享套餐"
        );
        mealSelector.setValue("不加購");

        // 座位選擇
        Button seatSelectBtn = createStyledButton("🪑 選擇座位");

        // 送出
        Button submitBtn = createStyledButton("✅ 確認訂票");
        submitBtn.setStyle(submitBtn.getStyle() + "-fx-background-color: #4caf50;");

        // 位置
        grid.add(createLabel("電影:"), 0, 0);
        grid.add(movieSelector, 1, 0);

        grid.add(createLabel("影城:"), 0, 1);
        grid.add(cinemaSelector, 1, 1);

        grid.add(createLabel("日期:"), 0, 2);
        grid.add(datePicker, 1, 2);

        grid.add(createLabel("時間:"), 0, 3);
        grid.add(timeSelector, 1, 3);

        grid.add(createLabel("格式:"), 0, 4);
        grid.add(formatSelector, 1, 4);

        grid.add(createLabel("票種:"), 0, 5);
        grid.add(ticketTypeSelector, 1, 5);

        grid.add(createLabel("數量:"), 0, 6);
        grid.add(ticketQuantity, 1, 6);

        grid.add(createLabel("身分證驗證:"), 0, 7);
        VBox idBox = new VBox(8, idField, idVerified);
        grid.add(idBox, 1, 7);

        grid.add(createLabel("優惠折扣:"), 0, 8);
        grid.add(discountSelector, 1, 8);

        grid.add(createLabel("配餐選擇:"), 0, 9);
        grid.add(mealSelector, 1, 9);

        grid.add(createLabel("座位:"), 0, 10);
        grid.add(seatSelectBtn, 1, 10);

        grid.add(createLabel("票價/餘票:"), 0, 11);
        VBox infoBox = new VBox(6, priceLabel, remainingLabel);
        grid.add(infoBox, 1, 11);

        form.getChildren().addAll(formTitle, grid, submitBtn);

        // ======= listeners: 更新票價、餘票、折扣清單 =======
        Runnable refreshPriceAndDiscount = () -> {
            String cinema = cinemaSelector.getValue();
            String fmt = formatSelector.getValue();
            String ticketTypeKey = mapTicketTypeKey(ticketTypeSelector.getValue());
            int qty = ticketQuantity.getValue();
            LocalDate showDate = datePicker.getValue();

            Integer base = priceService.getPrice(cinema, fmt, ticketTypeKey);
            if (base == null) {
                base = priceService.getLowestPrice(fmt, ticketTypeKey);
            }
            int basePrice = base == null || base < 0 ? 0 : base;

            boolean student = "STUDENT".equals(ticketTypeKey);
            boolean studentVerified = student && idVerified.isSelected() && userService.validateTaiwanId(idField.getText());

            PriceService.DiscountContext ctx = new PriceService.DiscountContext(showDate, qty,
                    userService.isLoggedIn(), studentVerified);

            // 刷新折扣
            String keep = discountSelector.getValue();
            discountSelector.getItems().setAll("不使用折扣");
            for (PriceService.Discount d : priceService.getApplicableDiscounts(ctx)) {
                discountSelector.getItems().add(d.code + " - " + d.name);
            }
            if (keep != null && discountSelector.getItems().contains(keep)) {
                discountSelector.setValue(keep);
            } else {
                discountSelector.setValue("不使用折扣");
            }

            // 計算總價
            String discountCode = extractDiscountCode(discountSelector.getValue());
            int unitAfterDiscount = basePrice;
            if (discountCode != null) {
                unitAfterDiscount = priceService.applyDiscount(basePrice, discountCode, ctx);
            }
            int total = unitAfterDiscount * qty;

            priceLabel.setText(String.format("票價：NT$ %d /張（%s）  |  總計：NT$ %d", unitAfterDiscount, fmt, total));

            // 餘票（以電影為單位簡化）
            String movieTitle = movieSelector.getValue();
            int remaining = bookingService.getRemaining(movieTitle);
            remainingLabel.setText("目前剩餘（示範）：" + remaining + " 張");
        };

        movieSelector.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        cinemaSelector.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        formatSelector.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        ticketTypeSelector.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        datePicker.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        ticketQuantity.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        idVerified.selectedProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());
        idField.textProperty().addListener((obs, o, n) -> {
            if (idVerified.isSelected()) refreshPriceAndDiscount.run();
        });
        discountSelector.valueProperty().addListener((obs, o, n) -> refreshPriceAndDiscount.run());

        refreshPriceAndDiscount.run();

        // ======= seat selection =======
        seatSelectBtn.setOnAction(e -> {
            String movie = movieSelector.getValue();
            String cinema = cinemaSelector.getValue();
            LocalDate d = datePicker.getValue();
            LocalTime t = LocalTime.parse(timeSelector.getValue());
            if (movie == null || cinema == null || d == null || t == null) {
                showAlert("❌ 請先選擇電影/影城/日期/時間");
                return;
            }
            int qty = ticketQuantity.getValue();
            showSeatSelection(movie, cinema, d, t, qty);
        });

        // ======= submit: payment + create order + e-ticket =======
        submitBtn.setOnAction(e -> {
            if (!userService.isLoggedIn()) {
                showAlert("❌ 請先登入才能訂票");
                return;
            }

            String movie = movieSelector.getValue();
            String cinema = cinemaSelector.getValue();
            LocalDate showDate = datePicker.getValue();
            LocalTime showTime = LocalTime.parse(timeSelector.getValue());
            String fmt = formatSelector.getValue();
            String ticketTypeText = ticketTypeSelector.getValue();
            String ticketTypeKey = mapTicketTypeKey(ticketTypeText);
            int qty = ticketQuantity.getValue();

            if (showDate == null || showDate.isBefore(LocalDate.now())) {
                showAlert("❌ 日期不可早於今天");
                return;
            }

            // 身分證驗證：要求勾選者必須通過
            String idNum = idField.getText() == null ? "" : idField.getText().trim();
            boolean idOk = userService.validateTaiwanId(idNum);
            if (idVerified.isSelected() && !idOk) {
                showAlert("❌ 身分證驗證失敗：請輸入正確身分證字號（A123456789）");
                return;
            }

            // 學生票：必須完成驗證
            if ("STUDENT".equals(ticketTypeKey) && !(idVerified.isSelected() && idOk)) {
                showAlert("❌ 選擇學生票需完成身分證驗證（示範：以身分證字號驗證）");
                return;
            }

            if (selectedSeats.size() != qty) {
                showAlert("❌ 座位數量必須等於購票張數\n\n目前座位：" + selectedSeats.size() + " / 張數：" + qty);
                return;
            }

            // 計價
            Integer base = priceService.getPrice(cinema, fmt, ticketTypeKey);
            if (base == null) base = priceService.getLowestPrice(fmt, ticketTypeKey);
            if (base == null || base < 0) {
                showAlert("❌ 票價資料不足，請更換影城/格式/票種");
                return;
            }

            boolean studentVerified = "STUDENT".equals(ticketTypeKey) && idVerified.isSelected() && idOk;
            PriceService.DiscountContext ctx = new PriceService.DiscountContext(showDate, qty,
                    userService.isLoggedIn(), studentVerified);

            String discountCode = extractDiscountCode(discountSelector.getValue());
            if (discountCode != null && !priceService.isDiscountApplicable(discountCode, ctx)) {
                showAlert("❌ 該折扣不符合使用條件，請重新選擇");
                return;
            }

            int unitAfter = (discountCode == null) ? base : priceService.applyDiscount(base, discountCode, ctx);
            int total = unitAfter * qty;

            String meal = mealSelector.getValue();

            // 付款
            PaymentResult pay = showPayment(total);
            if (!pay.success) return;

            // 建立訂單（座位會在此刻占用）
            BookingService.Booking booking = bookingService.createBooking(
                    userService.getCurrentUserId(),
                    movie,
                    cinema,
                    showDate,
                    showTime,
                    new ArrayList<>(selectedSeats),
                    total,
                    ticketTypeText,
                    discountCode,
                    meal,
                    pay.paymentMethod,
                    idVerified.isSelected() ? idNum.toUpperCase() : null
            );

            if (booking == null) {
                showAlert("❌ 訂單建立失敗：可能座位已被佔用或資料不完整\n\n請重新選位再試一次");
                return;
            }

            // 付款成功 -> 發電子票
            bookingService.confirmPayment(booking);

            showTicket(booking);

            // reset
            selectedSeats.clear();
            showAlert("✅ 訂票成功！\n\n訂單ID：" + booking.bookingId + "\n電子票券：" + booking.ticketCode);
            refreshPriceAndDiscount.run();
        });

        return form;
    }

    // =========================
    // Seat selection
    // =========================

    private void showSeatSelection(String movieTitle, String cinema, LocalDate date, LocalTime time, int ticketQty) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("選擇座位");
        dialog.setHeaderText("請選擇 " + ticketQty + " 個座位（可自動/手動）");

        selectedSeats.clear();

        String showKey = bookingService.buildShowKey(movieTitle, cinema, date, time);
        Set<String> booked = bookingService.getBookedSeats(showKey);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // 顯示資訊
        Label info = new Label("場次：" + movieTitle + " | " + cinema + " | " + date + " " + time);
        info.setStyle("-fx-text-fill: rgba(255,255,255,0.85);");

        // 座位區
        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
        seatGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < BookingService.SEAT_ROWS; row++) {
            char rowChar = (char) ('A' + row);
            for (int col = 1; col <= BookingService.SEAT_COLS; col++) {
                String seatId = rowChar + String.valueOf(col);
                Button seatBtn = createSeatButton(seatId);

                boolean isTaken = booked.contains(seatId);
                if (isTaken) {
                    seatBtn.setDisable(true);
                    seatBtn.setStyle("-fx-background-color: rgba(255,0,0,0.35); -fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11; -fx-cursor: default;");
                }

                seatBtn.setOnAction(e -> {
                    if (seatBtn.isDisabled()) return;
                    if (selectedSeats.contains(seatId)) {
                        selectedSeats.remove(seatId);
                        seatBtn.setStyle(getSeatStyle(false));
                    } else {
                        if (selectedSeats.size() >= ticketQty) {
                            showAlert("❌ 已達上限（" + ticketQty + "）\n\n請先取消一個座位");
                            return;
                        }
                        selectedSeats.add(seatId);
                        seatBtn.setStyle(getSeatStyle(true));
                    }
                });

                seatGrid.add(seatBtn, col - 1, row);
            }
        }

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button autoBtn = createStyledButton("✨ 自動選位");
        autoBtn.setOnAction(e -> autoSelectSeats(showKey, ticketQty, seatGrid));

        Button clearBtn = createStyledButton("🧹 清除");
        clearBtn.setOnAction(e -> {
            selectedSeats.clear();
            for (Node n : seatGrid.getChildren()) {
                if (n instanceof Button) {
                    Button b = (Button) n;
                    if (!b.isDisabled()) b.setStyle(getSeatStyle(false));
                }
            }
        });

        buttons.getChildren().addAll(autoBtn, clearBtn);

        // legend
        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendItem("可選", "-fx-background-color: rgba(50,184,198,0.2);") ,
                legendItem("已選", "-fx-background-color: #32b8c6;"),
                legendItem("已售", "-fx-background-color: rgba(255,0,0,0.35);")
        );

        content.getChildren().addAll(info, seatGrid, buttons, legend);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (selectedSeats.size() != ticketQty) {
                    showAlert("❌ 請選擇正確數量的座位\n\n目前：" + selectedSeats.size() + " / 需要：" + ticketQty);
                    return null;
                }
            }
            return button;
        });

        dialog.showAndWait();
    }

    private void autoSelectSeats(String showKey, int ticketQty, GridPane seatGrid) {
        selectedSeats.clear();

        // 先把手動選的顏色清掉
        for (Node n : seatGrid.getChildren()) {
            if (n instanceof Button) {
                Button b = (Button) n;
                if (!b.isDisabled()) b.setStyle(getSeatStyle(false));
            }
        }

        // 優先選同排連號
        outer:
        for (int row = 0; row < BookingService.SEAT_ROWS; row++) {
            char rowChar = (char) ('A' + row);
            int consecutive = 0;
            List<String> tmp = new ArrayList<>();

            for (int col = 1; col <= BookingService.SEAT_COLS; col++) {
                String seatId = rowChar + String.valueOf(col);
                if (bookingService.isSeatAvailable(showKey, seatId)) {
                    consecutive++;
                    tmp.add(seatId);
                    if (consecutive == ticketQty) {
                        selectedSeats.addAll(tmp);
                        break outer;
                    }
                } else {
                    consecutive = 0;
                    tmp.clear();
                }
            }
        }

        // 如果找不到連號，就隨機選
        if (selectedSeats.size() != ticketQty) {
            selectedSeats.clear();
            List<String> candidates = new ArrayList<>();
            for (int row = 0; row < BookingService.SEAT_ROWS; row++) {
                char rowChar = (char) ('A' + row);
                for (int col = 1; col <= BookingService.SEAT_COLS; col++) {
                    String seatId = rowChar + String.valueOf(col);
                    if (bookingService.isSeatAvailable(showKey, seatId)) candidates.add(seatId);
                }
            }
            Collections.shuffle(candidates);
            for (String s : candidates) {
                selectedSeats.add(s);
                if (selectedSeats.size() == ticketQty) break;
            }
        }

        // 上色
        for (Node n : seatGrid.getChildren()) {
            if (!(n instanceof Button)) continue;
            Button b = (Button) n;
            String seatId = b.getText();
            if (!b.isDisabled() && selectedSeats.contains(seatId)) {
                b.setStyle(getSeatStyle(true));
            }
        }
    }

    private HBox legendItem(String text, String style) {
        Label dot = new Label("  ");
        dot.setStyle(style + "-fx-min-width: 16; -fx-min-height: 12; -fx-border-radius: 3; -fx-background-radius: 3;");
        Label lb = new Label(text);
        lb.setStyle("-fx-text-fill: rgba(255,255,255,0.75);");
        return new HBox(6, dot, lb);
    }

    // =========================
    // Payment
    // =========================

    private static class PaymentResult {
        boolean success;
        String paymentMethod;

        PaymentResult(boolean success, String paymentMethod) {
            this.success = success;
            this.paymentMethod = paymentMethod;
        }
    }

    /**
     * 付款 + 信用卡驗證 + 交易限時
     * - 交易限時：180 秒（倒數，時間到自動取消）
     */
    private PaymentResult showPayment(int totalAmount) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("付款");
        dialog.setHeaderText("請完成付款（交易限時 180 秒）");

        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        Label amountLabel = new Label("應付金額：NT$ " + totalAmount);
        amountLabel.setStyle("-fx-text-fill: #32b8c6; -fx-font-weight: bold; -fx-font-size: 14;");

        Label timerLabel = new Label("剩餘時間：180 秒");
        timerLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");

        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton creditCard = new RadioButton("信用卡");
        creditCard.setToggleGroup(paymentGroup);
        creditCard.setSelected(true);
        creditCard.setStyle("-fx-text-fill: white;");

        RadioButton cash = new RadioButton("現場付款（示範）");
        cash.setToggleGroup(paymentGroup);
        cash.setStyle("-fx-text-fill: white;");

        VBox paymentOptions = new VBox(8, creditCard, cash);

        // 信用卡欄位
        TextField cardNumber = new TextField();
        cardNumber.setPromptText("卡號 (16位)");
        TextField exp = new TextField();
        exp.setPromptText("到期日 (MM/YY)");
        PasswordField cvv = new PasswordField();
        cvv.setPromptText("CVV (3位)");

        VBox cardBox = new VBox(8, cardNumber, exp, cvv);

        // 切換付款方式
        paymentGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean isCC = n == creditCard;
            cardBox.setDisable(!isCC);
            cardBox.setOpacity(isCC ? 1.0 : 0.4);
        });

        box.getChildren().addAll(amountLabel, timerLabel, paymentOptions, cardBox);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        // 交易倒數
        final int[] remain = {180};
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            remain[0]--;
            timerLabel.setText("剩餘時間：" + remain[0] + " 秒");
            if (remain[0] <= 0) {
                okBtn.setDisable(true);
                showAlert("⏰ 交易逾時，請重新操作");
                dialog.setResult(ButtonType.CANCEL);
                dialog.close();
            }
        }));
        tl.setCycleCount(180);
        tl.play();

        // OK 先驗證再關閉
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            boolean isCC = paymentGroup.getSelectedToggle() == creditCard;
            if (isCC) {
                String num = cardNumber.getText() == null ? "" : cardNumber.getText().replaceAll("\\s+", "");
                String expStr = exp.getText() == null ? "" : exp.getText().trim();
                String cvvStr = cvv.getText() == null ? "" : cvv.getText().trim();

                String err = validateCreditCard(num, expStr, cvvStr);
                if (err != null) {
                    showAlert("❌ 信用卡驗證失敗\n\n" + err);
                    ev.consume();
                }
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        tl.stop();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean isCC = paymentGroup.getSelectedToggle() == creditCard;
            return new PaymentResult(true, isCC ? "信用卡" : "現場付款");
        }
        return new PaymentResult(false, null);
    }

    private String validateCreditCard(String num, String expMMYY, String cvv) {
        if (num == null || !num.matches("^[0-9]{13,19}$")) {
            return "卡號需為 13~19 位數字";
        }
        if (!luhnCheck(num)) {
            return "卡號未通過 Luhn 驗證";
        }
        if (expMMYY == null || !expMMYY.matches("^(0[1-9]|1[0-2])\\/[0-9]{2}$")) {
            return "到期日格式需為 MM/YY";
        }
        if (cvv == null || !cvv.matches("^[0-9]{3}$")) {
            return "CVV 需為 3 位數字";
        }
        return null;
    }

    private boolean luhnCheck(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    // =========================
    // Ticket
    // =========================

    private void showTicket(BookingService.Booking booking) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("電子票券");

        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setStyle("-fx-control-inner-background: #1a2637; -fx-text-fill: rgba(255,255,255,0.9); " +
                "-fx-font-family: monospace; -fx-font-size: 12;");

        area.setText(
                "╔════════════════════════════════════╗\n" +
                        "║            電 子 票 券               ║\n" +
                        "╚════════════════════════════════════╝\n\n" +
                        "票券代碼：" + booking.ticketCode + "\n" +
                        "訂單 ID：" + booking.bookingId + "\n\n" +
                        "電影：" + booking.movieTitle + "\n" +
                        "影城：" + booking.cinema + "\n" +
                        "日期：" + booking.bookingDate + "\n" +
                        "時間：" + booking.bookingTime + "\n" +
                        "座位：" + String.join(", ", booking.seats) + "\n\n" +
                        "票種：" + (booking.ticketType == null ? "—" : booking.ticketType) + "\n" +
                        "優惠：" + (booking.discountCode == null ? "—" : booking.discountCode) + "\n" +
                        "配餐：" + (booking.meal == null ? "不加購" : booking.meal) + "\n\n" +
                        "金額：NT$ " + booking.totalPrice + "\n" +
                        "付款：" + (booking.paymentMethod == null ? "—" : booking.paymentMethod) + "\n\n" +
                        "※ 提醒：請提前 15 分鐘到場，憑票券代碼/截圖驗票入場。"
        );

        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // =========================
    // UI helper
    // =========================

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        return label;
    }

    private ComboBox<String> createStyledComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setStyle("-fx-font-size: 14; -fx-padding: 5;");
        combo.setPrefWidth(320);
        return combo;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-padding: 10 25; -fx-font-size: 14; -fx-font-weight: bold; " +
                "-fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    private Button createSeatButton(String seatId) {
        Button seat = new Button(seatId);
        seat.setPrefSize(45, 30);
        seat.setStyle(getSeatStyle(false));
        return seat;
    }

    private String getSeatStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: #32b8c6; -fx-text-fill: white; -fx-font-size: 11; -fx-border-radius: 4;";
        }
        return "-fx-background-color: rgba(50,184,198,0.2); -fx-text-fill: white; -fx-font-size: 11; -fx-border-radius: 4;";
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("提示");
        alert.getDialogPane().setStyle("-fx-background-color: #0b1220; -fx-text-fill: white;");
        alert.showAndWait();
    }

    private String mapTicketTypeKey(String uiText) {
        if (uiText == null) return "ADULT";
        switch (uiText) {
            case "學生票":
                return "STUDENT";
            case "敬老票":
                return "SENIOR";
            case "孩童票":
                return "CHILD";
            case "全票":
            default:
                return "ADULT";
        }
    }

    private String extractDiscountCode(String selection) {
        if (selection == null) return null;
        if (selection.equals("不使用折扣")) return null;
        int idx = selection.indexOf(" ");
        if (idx <= 0) return selection.trim();
        return selection.substring(0, idx).trim();
    }
}
