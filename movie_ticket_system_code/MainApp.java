package org.example.App;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.App.modules.*;
import org.example.App.services.BookingService;
import org.example.App.services.PriceService;
import org.example.App.services.UserService;

import java.util.List;

public class MainApp extends Application {

    private final BookingService bookingService = new BookingService();
    private final PriceService priceService = new PriceService();
    private final UserService userService = new UserService();

    private Stage primaryStage;
    private BorderPane mainLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("🎬 電影訂票系統");
        primaryStage.setScene(new Scene(showLoginPage(), 1100, 750));
        primaryStage.show();
    }

    // =========================
    // Login page
    // =========================

    private Scene showLoginPage() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("🎬 電影訂票系統");
        title.setStyle("-fx-font-size: 38; -fx-text-fill: #32b8c6; -fx-font-weight: bold;");

        VBox loginCard = new VBox(15);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(30));
        loginCard.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 20; " +
                "-fx-border-color: rgba(50,184,198,0.3); -fx-border-radius: 20;");

        TextField userField = new TextField();
        userField.setPromptText("使用者ID (例: user / admin)");
        userField.setMaxWidth(320);
        userField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("密碼 (例: 1234 / admin123)");
        passField.setMaxWidth(320);
        passField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        Button loginBtn = createPrimaryButton("🔐 登入");
        Button guestBtn = createSecondaryButton("👤 訪客模式");
        Button registerBtn = createSecondaryButton("📝 註冊");
        Button forgotBtn = createSecondaryButton("❓ 忘記密碼");

        loginBtn.setOnAction(e -> {
            if (userService.authenticate(userField.getText(), passField.getText())) {
                showMainPage();
            } else {
                showAlert("❌ 登入失敗", "帳號或密碼錯誤");
            }
        });

        guestBtn.setOnAction(e -> showMainPage());

        registerBtn.setOnAction(e -> showRegisterDialog());
        forgotBtn.setOnAction(e -> showForgotPasswordDialog());

        HBox row1 = new HBox(10, loginBtn, guestBtn);
        row1.setAlignment(Pos.CENTER);
        HBox row2 = new HBox(10, registerBtn, forgotBtn);
        row2.setAlignment(Pos.CENTER);

        loginCard.getChildren().addAll(userField, passField, row1, row2);

        Label note = new Label("提示：admin/admin123 為管理員；user/1234 為一般使用者。\n" +
                "本系統為示範版，忘記密碼會顯示重設碼（未寄信/簡訊）。");
        note.setStyle("-fx-text-fill: rgba(255,255,255,0.65);");
        note.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, loginCard, note);
        return new Scene(root);
    }

    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("註冊");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #0b1220;");

        TextField user = new TextField();
        user.setPromptText("userId");
        PasswordField pwd = new PasswordField();
        pwd.setPromptText("密碼(至少4碼)");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField phone = new TextField();
        phone.setPromptText("Phone");

        box.getChildren().addAll(label("帳號"), user, label("密碼"), pwd, label("Email"), email, label("Phone"), phone);
        dialog.getDialogPane().setContent(box);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            boolean success = userService.registerUser(user.getText(), pwd.getText(), email.getText(), phone.getText());
            if (!success) {
                showAlert("❌ 註冊失敗", "可能原因：帳號已存在、密碼太短、資料不完整。\n（密碼至少4碼）");
                ev.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showForgotPasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("忘記密碼");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #0b1220;");

        TextField userOrEmail = new TextField();
        userOrEmail.setPromptText("輸入 userId 或 Email");

        Button getCode = new Button("取得重設碼");
        Label codeLabel = new Label();
        codeLabel.setTextFill(Color.WHITE);

        TextField codeField = new TextField();
        codeField.setPromptText("輸入重設碼");

        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("新密碼(至少4碼)");

        Button confirm = new Button("確認重設");

        getCode.setOnAction(e -> {
            String code = userService.requestPasswordReset(userOrEmail.getText());
            if (code == null) {
                codeLabel.setText("找不到該帳號/Email");
                codeLabel.setTextFill(Color.SALMON);
            } else {
                codeLabel.setText("重設碼（示範顯示）：" + code);
                codeLabel.setTextFill(Color.LIGHTGREEN);
            }
        });

        confirm.setOnAction(e -> {
            boolean ok = userService.confirmPasswordReset(userOrEmail.getText(), codeField.getText(), newPwd.getText());
            if (ok) {
                showAlert("✅ 成功", "密碼已重設，請回登入頁重新登入。");
                dialog.close();
            } else {
                showAlert("❌ 失敗", "重設碼不正確或新密碼太短。");
            }
        });

        box.getChildren().addAll(
                label("帳號/Email"), userOrEmail,
                getCode, codeLabel,
                label("重設碼"), codeField,
                label("新密碼"), newPwd,
                confirm
        );
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    // =========================
    // Main page
    // =========================

    private void showMainPage() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #0b1220;");

        mainLayout.setTop(createTopBar());
        mainLayout.setLeft(createSideMenu());
        mainLayout.setCenter(showMovieRecommendation());

        Scene scene = new Scene(mainLayout, 1100, 750);
        primaryStage.setScene(scene);
    }

    private Node createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: rgba(26,38,55,0.9);");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("🎬 MovieBooking");
        logo.setStyle("-fx-font-size: 20; -fx-text-fill: #32b8c6; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userStatus;
        if (userService.isLoggedIn()) {
            userStatus = new Label("👤 " + userService.getCurrentUserId() + (userService.isCurrentUserAdmin() ? " (Admin)" : ""));
        } else {
            userStatus = new Label("👤 訪客");
        }
        userStatus.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14;");

        Button accountBtn = createSecondaryButton("🔐 帳號");
        accountBtn.setOnAction(e -> showAccountDialog());
        accountBtn.setDisable(!userService.isLoggedIn());

        Button logoutBtn = createSecondaryButton("🚪 登出");
        logoutBtn.setOnAction(e -> {
            userService.logout();
            primaryStage.setScene(showLoginPage());
        });
        logoutBtn.setDisable(!userService.isLoggedIn());

        topBar.getChildren().addAll(logo, spacer, userStatus, accountBtn, logoutBtn);
        return topBar;
    }

    private void showAccountDialog() {
        if (!userService.isLoggedIn()) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("帳號設定");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox box = new VBox(12);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #0b1220;");

        // 修改密碼
        PasswordField oldPwd = new PasswordField();
        oldPwd.setPromptText("舊密碼");
        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("新密碼(至少4碼)");

        // profile
        TextField area = new TextField(userService.getAreaOfCurrentUser());
        area.setPromptText("所在地區（台北/新北/桃園...）");

        TextField nid = new TextField(userService.getNationalIdOfCurrentUser() == null ? "" : userService.getNationalIdOfCurrentUser());
        nid.setPromptText("身分證字號（可選，用於身份驗證）");

        box.getChildren().addAll(
                label("修改密碼"), oldPwd, newPwd,
                new Separator(),
                label("所在地區（推薦用）"), area,
                label("身分證字號（可選）"), nid
        );

        dialog.getDialogPane().setContent(box);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            // 密碼：若有填才改
            if (!oldPwd.getText().isBlank() || !newPwd.getText().isBlank()) {
                boolean passOk = userService.changePassword(oldPwd.getText(), newPwd.getText());
                if (!passOk) {
                    showAlert("❌ 修改失敗", "舊密碼不正確或新密碼太短（至少4碼）。");
                    ev.consume();
                    return;
                }
            }

            // area
            if (!area.getText().isBlank()) {
                userService.setAreaForCurrentUser(area.getText());
            }

            // national id
            if (!nid.getText().isBlank()) {
                boolean idOk = userService.setNationalIdForCurrentUser(nid.getText());
                if (!idOk) {
                    showAlert("❌ 身分證錯誤", "身分證字號格式或檢查碼不正確。");
                    ev.consume();
                }
            }
        });

        dialog.showAndWait();
    }

    private Node createSideMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: rgba(26,38,55,0.8);");
        menu.setPrefWidth(200);

        Button recommendBtn = createMenuButton("🏠 推薦首頁");
        Button bookingBtn = createMenuButton("🎟️ 訂票");
        Button ordersBtn = createMenuButton("📦 訂單查詢");
        Button refundBtn = createMenuButton("↩️ 退票");
        Button pricingBtn = createMenuButton("💰 比價/優惠");
        Button adminBtn = createMenuButton("⚙️ 後臺管理");

        recommendBtn.setOnAction(e -> mainLayout.setCenter(showMovieRecommendation()));
        bookingBtn.setOnAction(e -> mainLayout.setCenter(new BookingModule(bookingService, priceService, userService).build()));
        ordersBtn.setOnAction(e -> mainLayout.setCenter(new OrderModule(bookingService, userService).build()));
        refundBtn.setOnAction(e -> mainLayout.setCenter(new RefundModule(bookingService, userService).build()));
        pricingBtn.setOnAction(e -> mainLayout.setCenter(new PriceModule(priceService, bookingService).build()));
        adminBtn.setOnAction(e -> mainLayout.setCenter(new AdminModule(userService, bookingService, priceService).build()));

        if (!userService.isLoggedIn() || !userService.isCurrentUserAdmin()) {
            adminBtn.setDisable(true);
            adminBtn.setTooltip(new Tooltip("只有管理員可使用"));
        }

        menu.getChildren().addAll(recommendBtn, bookingBtn, ordersBtn, refundBtn, pricingBtn, adminBtn);
        return menu;
    }

    // =========================
    // Recommendation page
    // =========================

    private Node showMovieRecommendation() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0b1220;");

        Label title = new Label("🌟 推薦");
        title.setStyle("-fx-font-size: 24; -fx-text-fill: white; -fx-font-weight: bold;");

        // 最近影城（示範：依使用者 area）
        String area = userService.isLoggedIn() ? userService.getAreaOfCurrentUser() : "台北";
        List<BookingService.Cinema> nearest = bookingService.getNearestCinemas(area, 3);

        VBox nearestBox = new VBox(6);
        nearestBox.setPadding(new Insets(12));
        nearestBox.setStyle("-fx-border-color: rgba(50,184,198,0.25); -fx-border-radius: 12;" +
                "-fx-background-color: rgba(26,38,55,0.75);");

        Label nearestTitle = new Label("📍 最近的電影院（示範） - 地區：" + area);
        nearestTitle.setStyle("-fx-text-fill: #32b8c6; -fx-font-weight: bold;");
        nearestBox.getChildren().add(nearestTitle);
        for (BookingService.Cinema c : nearest) {
            Label l = new Label("• " + c.name + "  |  " + c.address);
            l.setStyle("-fx-text-fill: rgba(255,255,255,0.85);");
            nearestBox.getChildren().add(l);
        }

        // 熱門電影
        VBox hotBox = new VBox(6);
        hotBox.setPadding(new Insets(12));
        hotBox.setStyle("-fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 12;" +
                "-fx-background-color: rgba(26,38,55,0.75);");
        Label hotTitle = new Label("🔥 最熱門的電影（依已付款訂單統計，無資料則用評分排序）");
        hotTitle.setStyle("-fx-text-fill: #ffb300; -fx-font-weight: bold;");
        hotBox.getChildren().add(hotTitle);
        int rank = 1;
        for (String t : bookingService.getMostPopularMovies(3)) {
            Label l = new Label(rank + ". " + t);
            l.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
            hotBox.getChildren().add(l);
            rank++;
        }

        // 搜尋電影
        TextField search = new TextField();
        search.setPromptText("搜尋電影名稱...");
        search.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        VBox listBox = new VBox(12);

        Runnable refreshList = () -> {
            listBox.getChildren().clear();
            String k = search.getText() == null ? "" : search.getText().trim();
            for (BookingService.Movie movie : bookingService.getMovies()) {
                if (!k.isEmpty() && !movie.title.contains(k)) continue;
                listBox.getChildren().add(createMovieCard(movie));
            }
            if (listBox.getChildren().isEmpty()) {
                Label empty = new Label("沒有符合條件的電影");
                empty.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");
                listBox.getChildren().add(empty);
            }
        };
        search.textProperty().addListener((o, a, b) -> refreshList.run());
        refreshList.run();

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().addAll(title, nearestBox, hotBox, search, scroll);
        return root;
    }

    private VBox createMovieCard(BookingService.Movie movie) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: rgba(26,38,55,0.8); -fx-background-radius: 12; " +
                "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 12;");

        Label movieTitle = new Label(movie.title);
        movieTitle.setStyle("-fx-font-size: 18; -fx-text-fill: white; -fx-font-weight: bold;");

        Label director = new Label("🎬 導演：" + movie.director);
        director.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");

        Label rating = new Label("⭐ 評分：" + movie.rating + " / 10");
        rating.setStyle("-fx-text-fill: #ffb300; -fx-font-weight: bold;");

        Label duration = new Label("⏱️ 片長：" + movie.duration + " 分鐘");
        duration.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");

        TextArea desc = new TextArea(movie.description);
        desc.setWrapText(true);
        desc.setEditable(false);
        desc.setPrefRowCount(2);
        desc.setStyle("-fx-control-inner-background: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                "-fx-border-color: transparent;");

        Button bookBtn = createPrimaryButton("立即訂票");
        bookBtn.setOnAction(e -> mainLayout.setCenter(new BookingModule(bookingService, priceService, userService).build()));

        card.getChildren().addAll(movieTitle, director, rating, duration, desc, bookBtn);
        return card;
    }

    // =========================
    // UI helpers
    // =========================

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(170);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; " +
                "-fx-padding: 12 15; -fx-alignment: center-left; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: rgba(50,184,198,0.15);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; " +
                "-fx-padding: 12 15; -fx-alignment: center-left; -fx-cursor: hand;"));
        return btn;
    }

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #32b8c6; -fx-text-fill: white; -fx-font-size: 14; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    private Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-font-size: 13; " +
                "-fx-font-weight: bold; -fx-padding: 10 18; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    private Label label(String t) {
        Label l = new Label(t);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
