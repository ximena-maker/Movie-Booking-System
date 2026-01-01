package org.example.App.modules;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.App.services.BookingService;
import org.example.App.services.PriceService;
import org.example.App.services.UserService;

import java.util.ArrayList;
import java.util.List;

public class AdminModule {
    private final BookingService bookingService;
    private final UserService userService;
    private final PriceService priceService;

    public AdminModule(UserService userService, BookingService bookingService, PriceService priceService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.priceService = priceService;
    }

    public static class BookingInfo {
        public String bookingId;
        public String userId;
        public String movieTitle;
        public String cinema;
        public String date;
        public int totalPrice;
        public String status;

        public BookingInfo(BookingService.Booking b) {
            this.bookingId = b.bookingId;
            this.userId = b.userId;
            this.movieTitle = b.movieTitle;
            this.cinema = b.cinema;
            this.date = b.bookingDate.toString() + " " + b.bookingTime;
            this.totalPrice = b.totalPrice;
            this.status = b.status;
        }

        public String getBookingId() { return bookingId; }
        public String getUserId() { return userId; }
        public String getMovieTitle() { return movieTitle; }
        public String getCinema() { return cinema; }
        public String getDate() { return date; }
        public int getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
    }

    public Node build() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("⚙️ 後臺管理系統");
        title.setStyle("-fx-font-size: 28; -fx-text-fill: #32b8c6; -fx-font-weight: bold;");

        if (!userService.isLoggedIn() || !userService.isCurrentUserAdmin()) {
            Label warn = new Label("❌ 只有管理員可以使用後台功能");
            warn.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14;");
            root.getChildren().addAll(title, warn);
            return new ScrollPane(root);
        }

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #0b1220; -fx-padding: 10;");

        tabs.getTabs().addAll(
                new Tab("📋 訂單管理", buildOrderManagement()),
                new Tab("🎬 電影/餘票", buildStockManagement()),
                new Tab("👥 用戶管理", buildUserManagement()),
                new Tab("📈 統計報表", buildStatistics())
        );

        root.getChildren().addAll(title, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        return new ScrollPane(root);
    }

    private Node buildOrderManagement() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #0b1220;");

        Label subtitle = new Label("所有訂單");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 16; -fx-font-weight: bold;");

        TableView<BookingInfo> table = new TableView<>();
        table.setStyle("-fx-background-color: #1a2637; -fx-text-fill: white;");

        TableColumn<BookingInfo, String> bookingCol = new TableColumn<>("訂單 ID");
        bookingCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookingCol.setPrefWidth(120);

        TableColumn<BookingInfo, String> userCol = new TableColumn<>("用戶 ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setPrefWidth(100);

        TableColumn<BookingInfo, String> movieCol = new TableColumn<>("電影");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(150);

        TableColumn<BookingInfo, String> cinemaCol = new TableColumn<>("影城");
        cinemaCol.setCellValueFactory(new PropertyValueFactory<>("cinema"));
        cinemaCol.setPrefWidth(130);

        TableColumn<BookingInfo, String> dateCol = new TableColumn<>("場次");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(160);

        TableColumn<BookingInfo, Integer> priceCol = new TableColumn<>("金額");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        priceCol.setPrefWidth(90);
        priceCol.setCellFactory(col -> new TableCell<BookingInfo, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : "NT$ " + item);
            }
        });

        TableColumn<BookingInfo, String> statusCol = new TableColumn<>("狀態");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(bookingCol, userCol, movieCol, cinemaCol, dateCol, priceCol, statusCol);

        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 5;");
        refreshBtn.setOnAction(e -> refreshOrderTable(table));

        refreshOrderTable(table);

        VBox content = new VBox(10, subtitle, refreshBtn, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return content;
    }

    private void refreshOrderTable(TableView<BookingInfo> table) {
        List<BookingInfo> bookingList = new ArrayList<>();
        for (BookingService.Booking b : bookingService.getAllBookings()) {
            bookingList.add(new BookingInfo(b));
        }
        table.getItems().setAll(bookingList);
    }

    private Node buildStockManagement() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #0b1220;");

        Label subtitle = new Label("電影列表 & 餘票（示範：以電影為單位）");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 16; -fx-font-weight: bold;");

        TableView<MovieStock> table = new TableView<>();
        table.setStyle("-fx-background-color: #1a2637; -fx-text-fill: white;");

        TableColumn<MovieStock, String> movieCol = new TableColumn<>("電影");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        movieCol.setPrefWidth(300);

        TableColumn<MovieStock, Integer> remainCol = new TableColumn<>("剩餘座位");
        remainCol.setCellValueFactory(new PropertyValueFactory<>("remaining"));
        remainCol.setPrefWidth(120);
        remainCol.setCellFactory(col -> new TableCell<MovieStock, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("—");
                    return;
                }
                setText(String.valueOf(item));
                if (item < 10) setStyle("-fx-text-fill: #ff8c00;");
                else setStyle("-fx-text-fill: #32b8c6;");
            }
        });

        table.getColumns().addAll(movieCol, remainCol);

        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 5;");
        refreshBtn.setOnAction(e -> reloadMovieStock(table));

        reloadMovieStock(table);

        VBox content = new VBox(10, subtitle, refreshBtn, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return content;
    }

    private void reloadMovieStock(TableView<MovieStock> table) {
        table.getItems().clear();
        for (BookingService.Movie m : bookingService.getMovies()) {
            table.getItems().add(new MovieStock(m.title, bookingService.getRemaining(m.title)));
        }
    }

    private Node buildUserManagement() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #0b1220;");

        Label subtitle = new Label("所有用戶");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 16; -fx-font-weight: bold;");

        TableView<UserInfo> table = new TableView<>();
        table.setStyle("-fx-background-color: #1a2637; -fx-text-fill: white;");

        TableColumn<UserInfo, String> userIdCol = new TableColumn<>("用戶 ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userIdCol.setPrefWidth(120);

        TableColumn<UserInfo, String> emailCol = new TableColumn<>("郵箱");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<UserInfo, String> phoneCol = new TableColumn<>("電話");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(140);

        TableColumn<UserInfo, String> roleCol = new TableColumn<>("角色");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(90);

        TableColumn<UserInfo, String> areaCol = new TableColumn<>("地區");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setPrefWidth(90);

        table.getColumns().addAll(userIdCol, emailCol, phoneCol, roleCol, areaCol);

        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #32b8c6; -fx-text-fill: white; -fx-border-radius: 5;");
        refreshBtn.setOnAction(e -> reloadUsers(table));

        reloadUsers(table);

        VBox content = new VBox(10, subtitle, refreshBtn, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return content;
    }

    private void reloadUsers(TableView<UserInfo> table) {
        table.getItems().clear();
        for (UserService.UserAccount user : userService.getAllUsers().values()) {
            table.getItems().add(new UserInfo(user));
        }
    }

    private Node buildStatistics() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #0b1220;");

        Label subtitle = new Label("統計概覽");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 16; -fx-font-weight: bold;");

        List<BookingService.Booking> allBookings = bookingService.getAllBookings();
        int totalOrders = allBookings.size();
        int confirmedOrders = (int) allBookings.stream().filter(b -> "已確認".equals(b.status)).count();
        int paidOrders = (int) allBookings.stream().filter(b -> "已付款".equals(b.status)).count();
        int refundedOrders = (int) allBookings.stream().filter(b -> "已退票".equals(b.status)).count();
        int totalRevenue = allBookings.stream().filter(b -> "已付款".equals(b.status)).mapToInt(b -> b.totalPrice).sum();
        int totalUsers = userService.getAllUsers().size();

        int avg = paidOrders > 0 ? (totalRevenue / paidOrders) : 0;

        Label stats = new Label(
                "📊 訂單統計\n" +
                        "─────────────────\n" +
                        "  總訂單數: " + totalOrders + "\n" +
                        "  已確認訂單: " + confirmedOrders + "\n" +
                        "  已付款訂單: " + paidOrders + "\n" +
                        "  已退票訂單: " + refundedOrders + "\n\n" +
                        "💰 營收統計\n" +
                        "─────────────────\n" +
                        "  總營收: NT$ " + totalRevenue + "\n" +
                        "  平均訂單金額: NT$ " + avg + "\n\n" +
                        "👥 用戶統計\n" +
                        "─────────────────\n" +
                        "  總用戶數: " + totalUsers
        );
        stats.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13; -fx-padding: 20; -fx-font-family: monospace;");
        stats.setWrapText(true);

        VBox statsBox = new VBox(stats);
        statsBox.setStyle("-fx-border-color: rgba(50,184,198,0.3); -fx-border-radius: 8; -fx-background-color: rgba(26,38,55,0.8); -fx-padding: 15;");

        box.getChildren().addAll(subtitle, statsBox);
        return box;
    }

    public static class MovieStock {
        public String title;
        public int remaining;

        public MovieStock(String title, int remaining) {
            this.title = title;
            this.remaining = remaining;
        }

        public String getTitle() { return title; }
        public int getRemaining() { return remaining; }
    }

    public static class UserInfo {
        public String userId;
        public String email;
        public String phone;
        public String role;
        public String area;

        public UserInfo(UserService.UserAccount user) {
            this.userId = user.userId;
            this.email = user.email;
            this.phone = user.phone;
            this.role = user.isAdmin ? "管理員" : "顧客";
            this.area = user.area;
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
        public String getArea() { return area; }
    }
}
