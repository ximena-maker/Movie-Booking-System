package org.example.App.modules;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.App.services.BookingService;
import org.example.App.services.PriceService;

import java.util.List;

/**
 * 比價/優惠展示模組
 */
public class PriceModule {

    private final PriceService priceService;
    private final BookingService bookingService;

    public PriceModule(PriceService priceService, BookingService bookingService) {
        this.priceService = priceService;
        this.bookingService = bookingService;
    }

    public Node build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("💰 比價 & 優惠");
        title.setStyle("-fx-font-size: 28; -fx-text-fill: #32b8c6; -fx-font-weight: bold;");

        // 選擇條件
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> format = new ComboBox<>();
        format.getItems().addAll("2D", "3D", "IMAX");
        format.setValue("2D");

        ComboBox<String> ticket = new ComboBox<>();
        ticket.getItems().addAll("ADULT", "STUDENT", "SENIOR", "CHILD");
        ticket.setValue("ADULT");

        controls.getChildren().addAll(
                label("格式"), styleCombo(format),
                label("票種"), styleCombo(ticket)
        );

        // 顯示區
        VBox result = new VBox(10);
        result.setPadding(new Insets(15));
        result.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 12;" +
                "-fx-background-color: rgba(26,38,55,0.8);");

        Label hint = new Label("選擇格式與票種後，系統會列出各影城票價並排序（由低到高）。");
        hint.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");

        TableView<PriceService.PriceQuote> table = new TableView<>();
        table.setPrefHeight(420);
        table.setStyle("-fx-background-color: #1a2637; -fx-text-fill: white;");

        TableColumn<PriceService.PriceQuote, String> cinemaCol = new TableColumn<>("影城");
        cinemaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().cinema));
        cinemaCol.setPrefWidth(180);

        TableColumn<PriceService.PriceQuote, String> formatCol = new TableColumn<>("格式");
        formatCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().format));
        formatCol.setPrefWidth(90);

        TableColumn<PriceService.PriceQuote, String> typeCol = new TableColumn<>("票種");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().ticketType));
        typeCol.setPrefWidth(100);

        TableColumn<PriceService.PriceQuote, String> priceCol = new TableColumn<>("單張票價");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("NT$ " + c.getValue().price));
        priceCol.setPrefWidth(120);

        TableColumn<PriceService.PriceQuote, String> srcCol = new TableColumn<>("來源");
        srcCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().source));
        srcCol.setPrefWidth(200);

        table.getColumns().addAll(cinemaCol, formatCol, typeCol, priceCol, srcCol);

        Label tips = new Label();
        tips.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

        Runnable refresh = () -> {
            List<PriceService.PriceQuote> quotes = priceService.compare(format.getValue(), ticket.getValue());
            table.getItems().setAll(quotes);
            int low = priceService.getLowestPrice(format.getValue(), ticket.getValue());
            tips.setText(low < 0
                    ? "目前沒有可比價資料"
                    : "最低票價：NT$ " + low + "（可在訂票頁套用折扣再降價）");
        };

        format.valueProperty().addListener((o, a, b) -> refresh.run());
        ticket.valueProperty().addListener((o, a, b) -> refresh.run());
        refresh.run();

        result.getChildren().addAll(hint, table, tips);

        root.getChildren().addAll(title, controls, result);
        return new ScrollPane(root);
    }

    private Label label(String text) {
        Label l = new Label(text + ":");
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-weight: bold;");
        return l;
    }

    private <T> ComboBox<T> styleCombo(ComboBox<T> combo) {
        combo.setStyle("-fx-font-size: 13; -fx-padding: 5;");
        combo.setPrefWidth(140);
        return combo;
    }
}
