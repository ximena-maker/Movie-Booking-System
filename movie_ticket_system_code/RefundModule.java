package org.example.App.modules;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.App.services.BookingService;
import org.example.App.services.UserService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class RefundModule {

    private BookingService bookingService;
    private UserService userService;

    public RefundModule(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    public Node build() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("↩️ 退票服務");
        title.setStyle("-fx-font-size: 28; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox infoBox = createInfoBox();
        root.getChildren().add(infoBox);

        VBox searchSection = createSearchSection();
        root.getChildren().add(searchSection);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setStyle("-fx-control-inner-background: #1a2637; -fx-text-fill: rgba(255,255,255,0.9); " +
                "-fx-padding: 10; -fx-font-family: monospace; -fx-font-size: 11;");
        resultArea.setPrefHeight(300);

        Button refundBtn = createRefundButton();
        refundBtn.setDisable(true);

        TextField bookingIdField = (TextField) searchSection.lookup("TextField");
        Button searchBtn = (Button) searchSection.lookup("Button");

        searchBtn.setOnAction(e -> handleSearch(bookingIdField, resultArea, refundBtn));
        refundBtn.setOnAction(e -> handleRefund(bookingIdField, resultArea, refundBtn));

        VBox form = new VBox(12);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12; " +
                "-fx-background-color: rgba(255,255,255,0.02);");

        Label resultLabel = new Label("📋 訂單詳情");
        resultLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13;");

        form.getChildren().addAll(resultLabel, resultArea, refundBtn);
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        root.getChildren().addAll(title, searchSection, form);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        return sp;
    }

    private VBox createInfoBox() {
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-border-color: rgba(76,175,80,0.25); -fx-border-radius: 10; " +
                "-fx-background-color: rgba(76,175,80,0.05);");

        Label infoTitle = new Label("💡 退票規則說明");
        infoTitle.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold; -fx-font-size: 13;");

        Label rule1 = new Label("✓ 演出前 7 天以上：全額退款 100%");
        rule1.setStyle("-fx-text-fill: white; -fx-font-size: 12;");

        Label rule2 = new Label("✓ 演出前 3-6 天：退款 80%");
        rule2.setStyle("-fx-text-fill: white; -fx-font-size: 12;");

        Label rule3 = new Label("✓ 演出前 1-2 天：退款 50%");
        rule3.setStyle("-fx-text-fill: white; -fx-font-size: 12;");

        Label rule4 = new Label("✗ 演出當日或已過期：不可退票");
        rule4.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12;");

        Label rule5 = new Label("ℹ️ 每筆退款會扣除退款金額的 10% 作為手續費");
        rule5.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11;");

        infoBox.getChildren().addAll(infoTitle, rule1, rule2, rule3, rule4, rule5);
        return infoBox;
    }

    private VBox createSearchSection() {
        VBox searchSection = new VBox(12);
        searchSection.setPadding(new Insets(20));
        searchSection.setStyle("-fx-border-color: rgba(50,184,198,0.3); -fx-border-radius: 10; " +
                "-fx-background-color: rgba(26,38,55,0.7);");

        Label searchTitle = new Label("🔍 查詢訂單");
        searchTitle.setStyle("-fx-text-fill: #32b8c6; -fx-font-weight: bold; -fx-font-size: 14;");

        HBox inputBox = new HBox(10);
        inputBox.setStyle("-fx-alignment: center-left;");

        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("輸入訂單 ID (例: BK1704107668000)");
        bookingIdField.setStyle("-fx-padding: 10; -fx-font-size: 13;");
        bookingIdField.setPrefWidth(350);

        Button searchBtn = createSearchButton();

        inputBox.getChildren().addAll(bookingIdField, searchBtn);
        HBox.setHgrow(bookingIdField, Priority.ALWAYS);

        searchSection.getChildren().addAll(searchTitle, inputBox);
        return searchSection;
    }

    private void handleSearch(TextField bookingIdField, TextArea resultArea, Button refundBtn) {
        String bookingId = bookingIdField.getText().trim();

        if (bookingId.isBlank()) {
            showAlert("❌ 請輸入訂單 ID");
            return;
        }

        for (BookingService.Booking b : bookingService.getAllBookings()) {
            if (b.bookingId.equals(bookingId)) {
                if (!userService.isLoggedIn() || !b.userId.equals(userService.getCurrentUserId())) {
                    showAlert("❌ 您沒有權限查詢此訂單");
                    refundBtn.setDisable(true);
                    return;
                }

                LocalDate showDate = b.bookingDate;
                LocalDate today = LocalDate.now();
                long daysUntilShow = ChronoUnit.DAYS.between(today, showDate);

                double refundRate = 0.0;
                String refundInfo = "";

                if (daysUntilShow >= 7) {
                    refundRate = 1.0;
                    refundInfo = "✓ 演出前 7 天以上：全額退款 100%";
                } else if (daysUntilShow >= 3) {
                    refundRate = 0.8;
                    refundInfo = "✓ 演出前 3-6 天：退款 80%";
                } else if (daysUntilShow >= 1) {
                    refundRate = 0.5;
                    refundInfo = "✓ 演出前 1-2 天：退款 50%";
                } else {
                    refundRate = 0.0;
                    refundInfo = "✗ 演出當日或已過期：不可退票";
                }

                int refundAmount = (int) (b.totalPrice * refundRate);
                int serviceFee = refundAmount / 10;
                int actualRefund = refundAmount - serviceFee;

                String details = String.format(
                        "╔═══════════════════════════════════════════╗\n" +
                                "║           退票訂單信息                      ║\n" +
                                "╚═══════════════════════════════════════════╝\n\n" +
                                "📋 訂單基本信息\n" +
                                "──────────────────────────────────────────\n" +
                                "  訂單 ID:  %s\n" +
                                "  狀態:    %s\n" +
                                "  電子票卷: %s\n\n" +
                                "🎬 電影信息\n" +
                                "──────────────────────────────────────────\n" +
                                "  片名:    %s\n" +
                                "  影城:    %s\n" +
                                "  日期:    %s\n" +
                                "  時間:    %s\n" +
                                "  座位:    %s\n\n" +
                                "💰 退款信息\n" +
                                "──────────────────────────────────────────\n" +
                                "  原購票金額:   NT$ %d\n" +
                                "  退款金額:    NT$ %d\n" +
                                "  手續費 (10%%): NT$ %d\n" +
                                "  實退金額:    NT$ %d\n" +
                                "  退款比例:    %d%%\n\n" +
                                "📅 退款規則\n" +
                                "──────────────────────────────────────────\n" +
                                "  演出日期: %s (距今 %d 天)\n" +
                                "  %s\n\n" +
                                "ℹ️  退票說明\n" +
                                "──────────────────────────────────────────\n" +
                                "  ✓ 退款將於 3-5 個工作天內退至原付款帳戶\n" +
                                "  ✓ 退票後無法再用該電子票卷進場\n" +
                                "  ✓ 請妥善保管電子票卷直到退票確認",

                        b.bookingId,
                        b.status,
                        b.ticketCode != null ? b.ticketCode : "未發放",
                        b.movieTitle,
                        b.cinema,
                        b.bookingDate,
                        b.bookingTime,
                        String.join(", ", b.seats),
                        b.totalPrice,
                        refundAmount,
                        serviceFee,
                        actualRefund,
                        (int)(refundRate * 100),
                        b.bookingDate,
                        daysUntilShow,
                        refundInfo
                );

                resultArea.setText(details);

                final double finalRefundRate = refundRate;
                final BookingService.Booking finalBooking = b;
                refundBtn.setDisable(finalRefundRate == 0.0);

                refundBtn.setUserData(new Object[]{finalBooking, refundAmount, serviceFee, actualRefund});

                return;
            }
        }

        showAlert("❌ 找不到該訂單\n\n請檢查訂單 ID 是否正確");
        resultArea.clear();
        refundBtn.setDisable(true);
    }

    private void handleRefund(TextField bookingIdField, TextArea resultArea, Button refundBtn) {
        Object[] data = (Object[]) refundBtn.getUserData();
        if (data == null) {
            showAlert("❌ 請先查詢訂單");
            return;
        }

        BookingService.Booking booking = (BookingService.Booking) data[0];
        int refundAmount = (int) data[1];
        int serviceFee = (int) data[2];
        int actualRefund = (int) data[3];

        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("確認退票");
        confirmDialog.getDialogPane().setPrefWidth(500);

        VBox confirmContent = new VBox(15);
        confirmContent.setPadding(new Insets(20));
        confirmContent.setStyle("-fx-background-color: #0b1220;");

        Label confirmTitle = new Label("⚠️ 確認退票");
        confirmTitle.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold; -fx-font-size: 14;");

        VBox detailsBox = new VBox(8);
        detailsBox.setPadding(new Insets(15));
        detailsBox.setStyle("-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; " +
                "-fx-background-color: rgba(255,255,255,0.02);");

        addConfirmRow(detailsBox, "訂單 ID", booking.bookingId);
        addConfirmRow(detailsBox, "電影名稱", booking.movieTitle);
        addConfirmRow(detailsBox, "原購票金額", "NT$ " + booking.totalPrice);
        addConfirmRow(detailsBox, "退款金額", "NT$ " + refundAmount, "#32b8c6");
        addConfirmRow(detailsBox, "手續費 (10%)", "NT$ " + serviceFee, "#ff9800");
        addConfirmRow(detailsBox, "實際退款", "NT$ " + actualRefund, "#4caf50");

        Label warningLabel = new Label("確定要退票嗎？退票後將無法使用電子票卷進場。");
        warningLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12; -fx-wrap-text: true;");
        warningLabel.setWrapText(true);

        confirmContent.getChildren().addAll(confirmTitle, detailsBox, warningLabel);

        confirmDialog.getDialogPane().setContent(confirmContent);
        confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        confirmDialog.getDialogPane().setStyle("-fx-background-color: #0b1220;");

        styleDialogButtons(confirmDialog);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            bookingService.refundBooking(booking.bookingId);

            showAlert("✅ 退票成功！\n\n" +
                    "原購票金額: NT$ " + booking.totalPrice + "\n" +
                    "退款金額: NT$ " + refundAmount + "\n" +
                    "手續費: NT$ " + serviceFee + "\n" +
                    "實退金額: NT$ " + actualRefund + "\n\n" +
                    "退款將於 3-5 個工作天內退至您的原付款帳戶");

            resultArea.clear();
            bookingIdField.clear();
            refundBtn.setDisable(true);
            refundBtn.setUserData(null);
        }
    }

    private void addConfirmRow(VBox container, String label, String value) {
        addConfirmRow(container, label, value, "white");
    }

    private void addConfirmRow(VBox container, String label, String value, String color) {
        HBox row = new HBox(15);
        row.setStyle("-fx-alignment: center-left;");

        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12; -fx-min-width: 100;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12; -fx-font-weight: bold;");

        row.getChildren().addAll(labelNode, valueNode);
        HBox.setHgrow(valueNode, Priority.ALWAYS);
        container.getChildren().add(row);
    }

    private Button createSearchButton() {
        Button btn = new Button("🔍 查詢");
        btn.setStyle("-fx-padding: 10 25; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-padding: 10 25; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: #2a9aa8; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-padding: 10 25; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;"));
        return btn;
    }

    private Button createRefundButton() {
        Button btn = new Button("✓ 確認退票");
        btn.setStyle("-fx-padding: 12 30; -fx-font-size: 14; -fx-font-weight: bold; " +
                "-fx-background-color: #c63530; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle("-fx-padding: 12 30; -fx-font-size: 14; -fx-font-weight: bold; " +
                        "-fx-background-color: #a82c27; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle("-fx-padding: 12 30; -fx-font-size: 14; -fx-font-weight: bold; " +
                        "-fx-background-color: #c63530; -fx-text-fill: white; -fx-border-radius: 5; -fx-cursor: hand;");
            }
        });
        return btn;
    }

    private void styleDialogButtons(Dialog<?> dialog) {
        dialog.getDialogPane().lookupButton(ButtonType.YES).setStyle(
                "-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #c63530; -fx-text-fill: white; -fx-font-weight: bold;");
        dialog.getDialogPane().lookupButton(ButtonType.NO).setStyle(
                "-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #5e5240; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("提示");

        alert.getDialogPane().setStyle(
                "-fx-background-color: #0b1220; " +
                        "-fx-text-fill: white;"
        );

        alert.getDialogPane().lookup(".header-panel").setStyle(
                "-fx-background-color: #0b1220; " +
                        "-fx-text-fill: white;"
        );

        alert.getDialogPane().lookup(".content.label").setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 14;"
        );

        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-padding: 8 20; " +
                        "-fx-background-color: #32b8c6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-radius: 4; " +
                        "-fx-cursor: hand;"
        );

        alert.showAndWait();
    }
}
