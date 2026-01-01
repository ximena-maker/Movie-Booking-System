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

    private final BookingService bookingService;
    private final UserService userService;

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

        // ✅ 不要 lookup：自己保留控制項參考
        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("輸入訂單 ID (例: BK1704107668000)");
        bookingIdField.setStyle("-fx-padding: 10; -fx-font-size: 13;");
        bookingIdField.setPrefWidth(350);

        Button searchBtn = createSearchButton();
        VBox searchSection = createSearchSection(bookingIdField, searchBtn);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setStyle("-fx-control-inner-background: #1a2637; -fx-text-fill: rgba(255,255,255,0.9); " +
                "-fx-padding: 10; -fx-font-family: monospace; -fx-font-size: 11;");
        resultArea.setPrefHeight(300);

        Button refundBtn = createRefundButton();
        refundBtn.setDisable(true);

        // ✅ 綁事件（不會 null）
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

        // ✅ 只 add 一次，順序也正確
        root.getChildren().addAll(title, infoBox, searchSection, form);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    private VBox createSearchSection(TextField bookingIdField, Button searchBtn) {
        VBox searchSection = new VBox(10);
        searchSection.setPadding(new Insets(20));
        searchSection.setStyle("-fx-border-color: rgba(50,184,198,0.3); -fx-border-radius: 10; " +
                "-fx-background-color: rgba(26,38,55,0.7);");

        Label searchTitle = new Label("🔍 查詢訂單");
        searchTitle.setStyle("-fx-text-fill: #32b8c6; -fx-font-weight: bold; -fx-font-size: 14;");

        HBox inputBox = new HBox(10);
        inputBox.setStyle("-fx-alignment: center-left;");
        inputBox.getChildren().addAll(bookingIdField, searchBtn);
        HBox.setHgrow(bookingIdField, Priority.ALWAYS);

        searchSection.getChildren().addAll(searchTitle, inputBox);
        return searchSection;
    }

    private VBox createInfoBox() {
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-border-color: rgba(76,175,80,0.25); -fx-border-radius: 10; " +
                "-fx-background-color: rgba(76,175,80,0.06);");

        Label t = new Label("✅ 退票規則（示範）");
        t.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");

        Label r1 = new Label("• 演出前 7 天以上：100%");
        Label r2 = new Label("• 演出前 3-6 天：80%");
        Label r3 = new Label("• 演出前 1-2 天：50%");
        Label r4 = new Label("• 當日/已過期：不可退票");
        for (Label l : new Label[]{r1, r2, r3, r4}) {
            l.setStyle("-fx-text-fill: rgba(255,255,255,0.85);");
        }

        infoBox.getChildren().addAll(t, r1, r2, r3, r4);
        return infoBox;
    }

    private Button createSearchButton() {
        Button b = new Button("查詢");
        b.setStyle("-fx-background-color: rgba(50,184,198,0.22); -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 10 18; -fx-background-radius: 8; -fx-cursor: hand;");
        return b;
    }

    private Button createRefundButton() {
        Button b = new Button("確認退票");
        b.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 22; -fx-background-radius: 8; -fx-cursor: hand;");
        return b;
    }

    private void handleSearch(TextField bookingIdField, TextArea resultArea, Button refundBtn) {
        String bookingId = bookingIdField.getText().trim();

        if (bookingId.isBlank()) {
            showAlert("❌ 請輸入訂單 ID");
            return;
        }

        for (BookingService.Booking b : bookingService.getAllBookings()) {
            if (b.bookingId.equals(bookingId)) {

                // ✅ 原本邏輯：必須登入且只能查自己的訂單（否則會擋）
                if (!userService.isLoggedIn() || !b.userId.equals(userService.getCurrentUserId())) {
                    showAlert("❌ 您沒有權限查詢此訂單");
                    refundBtn.setDisable(true);
                    return;
                }

                LocalDate showDate = b.bookingDate;
                LocalDate today = LocalDate.now();
                long daysUntilShow = ChronoUnit.DAYS.between(today, showDate);

                double refundRate;
                String refundInfo;

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
                        "訂單 ID: %s\n狀態: %s\n電影: %s\n影城: %s\n日期: %s %s\n座位: %s\n\n" +
                                "原金額: NT$ %d\n退款金額: NT$ %d\n手續費(10%%): NT$ %d\n實退: NT$ %d\n\n" +
                                "距今 %d 天\n%s",
                        b.bookingId, b.status, b.movieTitle, b.cinema,
                        b.bookingDate, b.bookingTime, String.join(", ", b.seats),
                        b.totalPrice, refundAmount, serviceFee, actualRefund,
                        daysUntilShow, refundInfo
                );

                resultArea.setText(details);

                refundBtn.setDisable(refundRate == 0.0);
                refundBtn.setUserData(new Object[]{b, refundAmount, serviceFee, actualRefund});
                return;
            }
        }

        showAlert("❌ 找不到該訂單，請檢查訂單 ID 是否正確");
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

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #0b1220;");

        Label msg = new Label("確定要退票嗎？退票後將無法使用電子票卷進場。");
        msg.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        Label d = new Label(
                "訂單: " + booking.bookingId +
                        "\n原金額: NT$ " + booking.totalPrice +
                        "\n退款金額: NT$ " + refundAmount +
                        "\n手續費: NT$ " + serviceFee +
                        "\n實退: NT$ " + actualRefund
        );
        d.setStyle("-fx-text-fill: rgba(255,255,255,0.85);");

        content.getChildren().addAll(msg, d);
        confirmDialog.getDialogPane().setContent(content);
        confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> res = confirmDialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            boolean ok = bookingService.refundBooking(booking.bookingId);
            if (ok) {
                showAlert("✅ 退票成功\n實退金額：NT$ " + actualRefund);
                resultArea.appendText("\n\n✅ 已完成退票（狀態已更新）");
                refundBtn.setDisable(true);
                refundBtn.setUserData(null);
            } else {
                showAlert("❌ 退票失敗（可能已退票或找不到訂單）");
            }
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("提示");
        a.setHeaderText(null);
        a.showAndWait();
    }
}
