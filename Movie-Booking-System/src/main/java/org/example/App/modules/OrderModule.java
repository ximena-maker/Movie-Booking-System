package org.example.App.modules;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.App.services.BookingService;
import org.example.App.services.UserService;

import java.util.List;

public class OrderModule {

    private final BookingService bookingService;
    private final UserService userService;

    public OrderModule(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    public Node build() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("📦 我的訂單");
        title.setStyle("-fx-font-size: 28; -fx-text-fill: white; -fx-font-weight: bold;");

        if (!userService.isLoggedIn()) {
            Label warn = new Label("請先登入才能查看訂單");
            warn.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14;");
            root.getChildren().addAll(title, warn);
            return new ScrollPane(root);
        }

        // 查詢
        HBox searchBox = new HBox(10);
        TextField keyword = new TextField();
        keyword.setPromptText("可輸入訂單ID / 電影名關鍵字");
        keyword.setStyle("-fx-padding: 10; -fx-font-size: 13;");
        Button searchBtn = new Button("🔍 查詢");
        searchBtn.setStyle("-fx-padding: 10 18; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;");
        searchBox.getChildren().addAll(keyword, searchBtn);
        HBox.setHgrow(keyword, Priority.ALWAYS);

        ListView<String> orderList = new ListView<>();
        orderList.setStyle("-fx-control-inner-background: #1a2637; -fx-text-fill: white;");
        orderList.setPrefHeight(450);

        TextArea detail = new TextArea();
        detail.setEditable(false);
        detail.setWrapText(true);
        detail.setStyle("-fx-control-inner-background: #1a2637; -fx-text-fill: rgba(255,255,255,0.9);");
        detail.setPrefHeight(220);

        Runnable refresh = () -> {
            orderList.getItems().clear();
            List<BookingService.Booking> bookings = bookingService.getUserBookings(userService.getCurrentUserId());
            String k = keyword.getText() == null ? "" : keyword.getText().trim();
            for (BookingService.Booking b : bookings) {
                if (!k.isEmpty()) {
                    boolean match = b.bookingId.contains(k) || (b.movieTitle != null && b.movieTitle.contains(k));
                    if (!match) continue;
                }
                orderList.getItems().add(String.format("%s | %s | %s %s | %s | NT$ %d",
                        b.bookingId,
                        b.movieTitle,
                        b.bookingDate,
                        b.bookingTime,
                        b.status,
                        b.totalPrice
                ));
            }
            if (orderList.getItems().isEmpty()) {
                orderList.getItems().add("目前沒有符合條件的訂單");
            }
        };

        searchBtn.setOnAction(e -> refresh.run());

        orderList.getSelectionModel().selectedItemProperty().addListener((obs, o, selected) -> {
            if (selected == null) return;
            if (selected.equals("目前沒有符合條件的訂單")) {
                detail.clear();
                return;
            }
            String bookingId = selected.split("\\|")[0].trim();
            BookingService.Booking b = bookingService.getBookingById(bookingId);
            if (b == null) {
                detail.setText("找不到訂單資料");
                return;
            }
            detail.setText(formatBookingDetails(b));
        });

        refresh.run();

        root.getChildren().addAll(title, searchBox, orderList, new Label("📄 訂單詳情"), detail);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    private String formatBookingDetails(BookingService.Booking b) {
        return String.format(
                "訂單ID: %s\n" +
                        "用戶: %s\n" +
                        "電影: %s\n" +
                        "影城: %s\n" +
                        "場次: %s %s\n" +
                        "座位: %s\n" +
                        "票種: %s\n" +
                        "折扣: %s\n" +
                        "配餐: %s\n" +
                        "付款方式: %s\n" +
                        "狀態: %s\n" +
                        "電子票券: %s\n" +
                        "金額: NT$ %d\n" +
                        "建立時間: %s\n" +
                        "付款時間: %s\n" +
                        "退票時間: %s\n",
                b.bookingId,
                b.userId,
                b.movieTitle,
                b.cinema,
                b.bookingDate,
                b.bookingTime,
                b.seats == null ? "—" : String.join(", ", b.seats),
                b.ticketType == null ? "—" : b.ticketType,
                b.discountCode == null ? "—" : b.discountCode,
                b.meal == null ? "—" : b.meal,
                b.paymentMethod == null ? "—" : b.paymentMethod,
                b.status,
                b.ticketCode == null ? "未發放" : b.ticketCode,
                b.totalPrice,
                b.createdAt == null ? "—" : b.createdAt,
                b.paidAt == null ? "—" : b.paidAt,
                b.refundedAt == null ? "—" : b.refundedAt
        );
    }
}
