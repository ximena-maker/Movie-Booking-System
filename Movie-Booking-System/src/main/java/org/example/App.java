package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.example.data.DataStore;
import org.example.model.*;
import org.example.service.*;

import java.util.*;

public class App extends Application {

    private Stage stage;

    // ====== Data + Services（沿用你的分層）======
    private final DataStore db = DataStore.bootstrapSample();

    private final AuthService auth = new AuthService(db);
    private final IDValidator idValidator = new IDValidator();
    private final SearchService search = new SearchService(db);
    private final BookingService bookingService = new BookingService(db);
    private final RefundService refundService = new RefundService(db);
    private final PriceCompareService priceCompareService = new PriceCompareService(db);
    private final RecommendationService recommendationService = new RecommendationService(db);

    // ====== Session ======
    private User currentUser = null;

    // ====== Booking flow state ======
    private Movie selectedMovie = null;
    private Showtime selectedShowtime = null;
    private TicketType selectedTicketType = TicketType.ADULT;
    private MealCombo selectedMeal = MealCombo.NONE;
    private String discountCode = null;
    private final Set<String> selectedSeats = new LinkedHashSet<>();
    private Booking currentBooking = null;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("電影訂票系統 (JavaFX)");
        stage.setScene(buildLoginScene());
        stage.setWidth(1100);
        stage.setHeight(720);
        stage.show();
    }

    // =========================
    // Login Scene
    // =========================
    private Scene buildLoginScene() {
        Label title = new Label("電影訂票系統");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        TextField tfUser = new TextField();
        tfUser.setPromptText("帳號");

        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("密碼");

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #d32f2f;");

        Button btnLogin = new Button("登入");
        btnLogin.setDefaultButton(true);

        Button btnForgot = new Button("忘記密碼");
        Button btnBrowse = new Button("不登入先瀏覽/比價/推薦");

        btnLogin.setOnAction(e -> {
            // 你原本應該是 Optional<User> login(...)
            var uOpt = auth.login(tfUser.getText().trim(), pfPass.getText());
            if (uOpt.isEmpty()) {
                msg.setText("登入失敗：帳密錯誤或帳號不存在");
                return;
            }
            currentUser = uOpt.get();
            resetBookingFlow();
            stage.setScene(buildMainScene());
        });

        btnForgot.setOnAction(e -> {
            String username = tfUser.getText().trim();
            if (username.isEmpty()) {
                showInfo("忘記密碼", "請先在帳號欄輸入帳號。");
                return;
            }
            String newPwd = auth.forgotPassword(username);
            if (newPwd == null) showInfo("忘記密碼", "找不到該帳號。");
            else showInfo("忘記密碼", "已重設新密碼（示範）：\n" + newPwd + "\n(正式版應寄Email/簡訊/OTP)");
        });

        btnBrowse.setOnAction(e -> {
            currentUser = null;
            resetBookingFlow();
            stage.setScene(buildMainScene());
        });

        VBox box = new VBox(12, title, tfUser, pfPass, btnLogin, btnForgot, new Separator(), btnBrowse, msg);
        box.setPadding(new Insets(28));
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(360);

        BorderPane root = new BorderPane(box);
        root.setPadding(new Insets(18));
        return new Scene(root);
    }

    // =========================
    // Main Scene (Tabs)
    // =========================
    private Scene buildMainScene() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabBook = new Tab("訂票/查詢");
        tabBook.setContent(buildBookingTab());

        Tab tabOrders = new Tab("我的訂單/退票");
        if (currentUser == null) {
            tabOrders.setContent(buildLoginRequiredPane("請先登入才能查看訂單與退票。"));
            tabOrders.setDisable(true);
        } else {
            tabOrders.setContent(buildOrdersTab());
        }



        Tab tabCompare = new Tab("比價");
        tabCompare.setContent(buildCompareTab());

        Tab tabReco = new Tab("推薦");
        tabReco.setContent(buildRecommendTab());

        Tab tabAccount = new Tab("帳號");
        if (currentUser == null) {
            tabAccount.setContent(buildLoginRequiredPane("請先登入才能使用帳號功能。"));
            tabAccount.setDisable(true);
        } else {
            tabAccount.setContent(buildAccountTab());
        }


        tabs.getTabs().addAll(tabBook, tabOrders, tabCompare, tabReco, tabAccount);

        if (currentUser == null) {
            tabOrders.setDisable(true);
            tabAccount.setDisable(true);
        }

        BorderPane root = new BorderPane(tabs);
        root.setTop(buildTopBar());
        return new Scene(root);
    }

    private HBox buildTopBar() {
        Label who = new Label(currentUser == null ? "目前：未登入（可查詢/比價/推薦）" : "目前登入：" + currentUser.username());
        who.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnLogout = new Button(currentUser == null ? "回登入" : "登出");
        btnLogout.setOnAction(e -> {
            currentUser = null;
            resetBookingFlow();
            stage.setScene(buildLoginScene());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(12, who, spacer, btnLogout);
        bar.setPadding(new Insets(10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #f3f6fb;");
        return bar;
    }

    // =========================
    // Booking Tab
    // =========================
    private Pane buildBookingTab() {
        // Left: Movie search + list
        TextField tfKw = new TextField();
        tfKw.setPromptText("輸入電影關鍵字");
        Button btnSearch = new Button("搜尋");

        ListView<Movie> lvMovies = new ListView<>();
        lvMovies.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Movie m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? "" : m.movieId() + " - " + m.title() + " (" + m.rating() + ")");
            }
        });

        btnSearch.setOnAction(e -> lvMovies.getItems().setAll(search.searchMoviesByTitle(tfKw.getText())));

        // Right: showtimes + controls + seat grid
        ListView<Showtime> lvShowtimes = new ListView<>();
        lvShowtimes.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Showtime s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(""); return; }
                Theater th = db.theaters.get(s.theaterId());
                setText(s.showtimeId() + " | " + th.name() + " | " + s.startTime() + " | 基本 $" + s.basePrice());
            }
        });

        lvMovies.getSelectionModel().selectedItemProperty().addListener((obs, o, m) -> {
            selectedMovie = m;
            selectedShowtime = null;
            selectedSeats.clear();
            currentBooking = null;
            if (m == null) {
                lvShowtimes.getItems().clear();
                return;
            }
            lvShowtimes.getItems().setAll(search.listShowtimesByMovie(m.movieId()));
        });

        lvShowtimes.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            selectedShowtime = s;
            selectedSeats.clear();
        });

        // Ticket type / meal / discount / id / qty
        ComboBox<TicketType> cbTicket = new ComboBox<>();
        cbTicket.getItems().addAll(TicketType.values());
        cbTicket.setValue(TicketType.ADULT);
        cbTicket.valueProperty().addListener((obs, o, v) -> selectedTicketType = v);

        ComboBox<MealCombo> cbMeal = new ComboBox<>();
        cbMeal.getItems().addAll(MealCombo.values());
        cbMeal.setValue(MealCombo.NONE);
        cbMeal.valueProperty().addListener((obs, o, v) -> selectedMeal = v);

        TextField tfDiscount = new TextField();
        tfDiscount.setPromptText("折扣碼 (OFF50 / OFF100)");

        TextField tfTWId = new TextField();
        tfTWId.setPromptText("身分證字號（訂票需驗證）");

        Spinner<Integer> spQty = new Spinner<>(1, 6, 1);

        // Seat grid
        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(6);
        seatGrid.setVgap(6);
        seatGrid.setPadding(new Insets(8));
        seatGrid.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0;");

        Button btnLoadSeats = new Button("載入座位圖");
        Button btnAutoSeat = new Button("自動選位");
        Button btnClearSeat = new Button("清除已選");

        btnLoadSeats.setOnAction(e -> {
            if (selectedShowtime == null) { showInfo("提示", "請先選擇場次。"); return; }
            rebuildSeatGrid(seatGrid, selectedShowtime.showtimeId());
        });

        btnAutoSeat.setOnAction(e -> {
            if (selectedShowtime == null) { showInfo("提示", "請先選擇場次。"); return; }
            int qty = spQty.getValue();
            var auto = bookingService.autoSelectSeats(selectedShowtime.showtimeId(), qty);
            if (auto.isEmpty()) { showInfo("自動選位", "座位不足或無法自動選位。"); return; }
            selectedSeats.clear();
            selectedSeats.addAll(auto);
            rebuildSeatGrid(seatGrid, selectedShowtime.showtimeId());
        });

        btnClearSeat.setOnAction(e -> {
            selectedSeats.clear();
            if (selectedShowtime != null) rebuildSeatGrid(seatGrid, selectedShowtime.showtimeId());
        });

        HBox seatBtns = new HBox(10, btnLoadSeats, btnAutoSeat, btnClearSeat);

        Label orderSummary = new Label("尚未建立訂單");
        orderSummary.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnCreateOrder = new Button("建立訂單");
        Button btnPay = new Button("付款 → 生成電子票券");
        btnPay.setDisable(true);

        btnCreateOrder.setOnAction(e -> {
            if (currentUser == null) { showInfo("需要登入", "訂票需要登入帳號。"); return; }
            if (selectedShowtime == null) { showInfo("提示", "請先選擇場次。"); return; }

            String id = tfTWId.getText().trim();
            if (!idValidator.isValidTWId(id)) { showInfo("身分證驗證失敗", "請輸入正確身分證字號。"); return; }

            int qty = spQty.getValue();
            if (selectedSeats.size() != qty) {
                showInfo("選位錯誤", "張數=" + qty + "，但已選座位數=" + selectedSeats.size());
                return;
            }

            discountCode = tfDiscount.getText().trim();
            if (discountCode.isEmpty()) discountCode = null;

            Booking b = bookingService.createBooking(
                    currentUser.userId(),
                    selectedShowtime.showtimeId(),
                    new ArrayList<>(selectedSeats),
                    selectedTicketType,
                    selectedMeal,
                    discountCode
            );

            if (b == null) {
                showInfo("建立訂單失敗", "可能：座位被搶/折扣碼無效");
                rebuildSeatGrid(seatGrid, selectedShowtime.showtimeId());
                return;
            }

            currentBooking = b;
            orderSummary.setText("訂單已建立：" + b.bookingId() + " | 應付 $" + b.totalPrice() + " | 狀態 " + b.status());
            btnPay.setDisable(false);
        });

        btnPay.setOnAction(e -> {
            if (currentBooking == null) { showInfo("提示", "請先建立訂單。"); return; }

            PaymentDialogResult r = showPaymentDialog(currentBooking);
            if (!r.success) {
                showInfo("付款失敗", r.message);
                bookingService.cancelUnpaid(currentBooking.bookingId());
                currentBooking = null;
                btnPay.setDisable(true);
                orderSummary.setText("付款失敗：訂單已取消，座位已釋放");
                rebuildSeatGrid(seatGrid, selectedShowtime.showtimeId());
                return;
            }

            var tickets = bookingService.getTicketsByBooking(currentBooking.bookingId());
            StringBuilder sb = new StringBuilder("付款成功！電子票券：\n\n");
            for (Ticket t : tickets) {
                sb.append("Seat ").append(t.seatId()).append(" | ").append(t.eTicketCode()).append("\n");
            }
            showInfo("電子票券", sb.toString());

            btnPay.setDisable(true);
            rebuildSeatGrid(seatGrid, selectedShowtime.showtimeId());
        });

        VBox left = new VBox(10, new Label("電影查詢"), new HBox(10, tfKw, btnSearch), lvMovies);
        left.setPadding(new Insets(10));
        left.setPrefWidth(380);

        VBox right = new VBox(10,
                new Label("場次清單"),
                lvShowtimes,
                new Separator(),
                new HBox(10, new Label("票種"), cbTicket, new Label("配餐"), cbMeal),
                new HBox(10, new Label("張數"), spQty),
                tfTWId,
                tfDiscount,
                seatBtns,
                seatGrid,
                new Separator(),
                orderSummary,
                new HBox(10, btnCreateOrder, btnPay)
        );
        right.setPadding(new Insets(10));
        right.setPrefWidth(680);

        HBox root = new HBox(left, new Separator(), right);
        HBox.setHgrow(right, Priority.ALWAYS);

        // 初始載入電影清單
        lvMovies.getItems().setAll(db.movies.values());
        return root;
    }

    private void rebuildSeatGrid(GridPane grid, String showtimeId) {
        grid.getChildren().clear();
        Map<String, Boolean> seats = db.seatMap.get(showtimeId);
        if (seats == null) return;

        // header
        grid.add(new Label(" "), 0, 0);
        for (int col = 1; col <= 10; col++) grid.add(new Label(String.valueOf(col)), col, 0);

        char[] rows = {'A','B'};
        for (int r = 0; r < rows.length; r++) {
            grid.add(new Label(String.valueOf(rows[r])), 0, r + 1);
            for (int c = 1; c <= 10; c++) {
                String seatId = rows[r] + String.valueOf(c);
                boolean available = Boolean.TRUE.equals(seats.get(seatId));

                ToggleButton btn = new ToggleButton(seatId);
                btn.setMinWidth(54);

                if (!available) {
                    btn.setDisable(true);
                    btn.setStyle("-fx-opacity: 0.65;");
                }

                btn.setSelected(selectedSeats.contains(seatId));
                btn.setOnAction(e -> {
                    if (btn.isSelected()) selectedSeats.add(seatId);
                    else selectedSeats.remove(seatId);
                });

                grid.add(btn, c, r + 1);
            }
        }
    }

    // =========================
    // Orders / Refund Tab
    // =========================
    private Pane buildOrdersTab() {
        // ✅ 未登入：不允許讀 currentUser，直接顯示提示畫面
        if (currentUser == null) {
            Label tip = new Label("請先登入才能查看訂單與退票。");
            Button btnGoLogin = new Button("回登入");
            btnGoLogin.setOnAction(e -> stage.setScene(buildLoginScene()));

            VBox box = new VBox(12, tip, btnGoLogin);
            box.setPadding(new Insets(16));
            box.setAlignment(Pos.CENTER);
            return box;
        }

        Label hint = new Label("已付款(PAID)的訂單可退票，退票後座位釋放。");
        ListView<Booking> lv = new ListView<>();
        lv.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Booking b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? "" :
                        b.bookingId() + " | " + b.status() + " | $" + b.totalPrice() + " | seats=" + b.seatIds());
            }
        });

        Button btnRefresh = new Button("重新整理");
        Button btnRefund = new Button("退票");

        Runnable refresh = () -> lv.getItems().setAll(bookingService.listBookingsByUser(currentUser.userId()));

        btnRefresh.setOnAction(e -> refresh.run());

        btnRefund.setOnAction(e -> {
            Booking b = lv.getSelectionModel().getSelectedItem();
            if (b == null) { showInfo("提示", "請先選一筆訂單"); return; }
            boolean ok = refundService.refund(b.bookingId());
            showInfo("退票結果", ok ? "退票成功" : "退票失敗（未付款/已退/不存在）");
            refresh.run();
        });

        VBox root = new VBox(10, hint, new HBox(10, btnRefresh, btnRefund), lv);
        root.setPadding(new Insets(12));

        // ✅ 進頁就先載入
        refresh.run();

        return root;
    }


    // =========================
    // Compare Tab
    // =========================
    private Pane buildCompareTab() {
        ComboBox<Movie> cb = new ComboBox<>();
        cb.getItems().setAll(db.movies.values());

        ListView<String> lv = new ListView<>();
        Button btn = new Button("比價（低→高）");
        btn.setOnAction(e -> {
            Movie m = cb.getValue();
            if (m == null) { showInfo("提示", "請選電影"); return; }
            lv.getItems().setAll(priceCompareService.compareByMovie(m.movieId()));
        });

        VBox root = new VBox(10, new Label("選電影比價不同影城/場次"), cb, btn, lv);
        root.setPadding(new Insets(12));
        return root;
    }

    // =========================
    // Recommend Tab
    // =========================
    private Pane buildRecommendTab() {
        TextField tfX = new TextField(); tfX.setPromptText("你的 X (例 12)");
        TextField tfY = new TextField(); tfY.setPromptText("你的 Y (例 9)");
        Label nearest = new Label("最近電影院：");
        Label hottest = new Label("最熱門電影：");

        Button btn = new Button("生成推薦");
        btn.setOnAction(e -> {
            try {
                double x = Double.parseDouble(tfX.getText().trim());
                double y = Double.parseDouble(tfY.getText().trim());
                Theater th = recommendationService.nearestTheater(x, y);
                Movie hot = recommendationService.hottestMovie();
                nearest.setText("最近電影院：" + th.name());
                hottest.setText("最熱門電影：" + hot.title());
            } catch (Exception ex) {
                showInfo("輸入錯誤", "座標請輸入數字");
            }
        });

        VBox root = new VBox(10,
                new Label("最近電影院（用座標距離示範）"),
                new HBox(10, tfX, tfY, btn),
                new Separator(),
                nearest,
                hottest
        );
        root.setPadding(new Insets(12));
        return root;
    }

    // =========================
    // Account Tab
    // =========================
    private Pane buildAccountTab() {
        if (currentUser == null) {
            return buildLoginRequiredPane("請先登入才能使用帳號功能。");
        }
        Label who = new Label("登入帳號：" + currentUser.username());

        PasswordField pfOld = new PasswordField(); pfOld.setPromptText("舊密碼");
        PasswordField pfNew = new PasswordField(); pfNew.setPromptText("新密碼");

        Button btn = new Button("修改密碼");
        btn.setOnAction(e -> {
            boolean ok = auth.changePassword(currentUser.username(), pfOld.getText(), pfNew.getText());
            showInfo("修改密碼", ok ? "修改成功" : "修改失敗：舊密碼錯誤");
            pfOld.clear(); pfNew.clear();
        });

        VBox root = new VBox(12, who, new Separator(), pfOld, pfNew, btn);
        root.setPadding(new Insets(12));
        return root;
    }

    // =========================
    // Payment Dialog
    // =========================
    private PaymentDialogResult showPaymentDialog(Booking booking) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("付款（信用卡驗證）");

        Label amt = new Label("應付金額：$" + booking.totalPrice());
        TextField tfCard = new TextField(); tfCard.setPromptText("信用卡號");
        TextField tfExp = new TextField(); tfExp.setPromptText("到期 YYYY-MM");
        PasswordField tfCvv = new PasswordField(); tfCvv.setPromptText("CVV(3碼)");
        Label msg = new Label(); msg.setStyle("-fx-text-fill: #d32f2f;");

        final PaymentDialogResult[] result = {new PaymentDialogResult(false, "使用者取消")};

        Button btnPay = new Button("確認付款");
        Button btnCancel = new Button("取消");

        btnPay.setOnAction(e -> {
            PaymentResult pr = bookingService.pay(booking.bookingId(), tfCard.getText(), tfExp.getText(), tfCvv.getText());
            if (!pr.success()) {
                msg.setText(pr.message());
                return;
            }
            result[0] = new PaymentDialogResult(true, "付款成功");
            dialog.close();
        });

        btnCancel.setOnAction(e -> {
            result[0] = new PaymentDialogResult(false, "使用者取消付款");
            dialog.close();
        });

        VBox root = new VBox(10, amt, tfCard, tfExp, tfCvv, msg, new HBox(10, btnPay, btnCancel));
        ((HBox)root.getChildren().get(root.getChildren().size()-1)).setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(14));

        dialog.setScene(new Scene(root, 420, 260));
        dialog.showAndWait();
        return result[0];
    }

    private void showInfo(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
    private Pane buildLoginRequiredPane(String text) {
        Label tip = new Label(text);
        Button btnGoLogin = new Button("回登入");
        btnGoLogin.setOnAction(e -> stage.setScene(buildLoginScene()));

        VBox box = new VBox(12, tip, btnGoLogin);
        box.setPadding(new Insets(16));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void resetBookingFlow() {
        selectedMovie = null;
        selectedShowtime = null;
        selectedTicketType = TicketType.ADULT;
        selectedMeal = MealCombo.NONE;
        discountCode = null;
        selectedSeats.clear();
        currentBooking = null;
    }

    private record PaymentDialogResult(boolean success, String message) {}

    public static void main(String[] args) {
        launch(args);
    }
}
