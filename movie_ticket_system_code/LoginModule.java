package org.example.App.modules;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.App.services.UserService;

/**
 * 獨立的登入/註冊/忘記密碼畫面（示範）
 * MainApp 已內建登入頁，所以此模組可視需求使用。
 */
public class LoginModule {

    private final UserService userService;

    public LoginModule(UserService userService) {
        this.userService = userService;
    }

    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab loginTab = new Tab("登入", buildLogin());
        Tab registerTab = new Tab("註冊", buildRegister());
        Tab forgotTab = new Tab("忘記密碼", buildForgot());

        tabs.getTabs().addAll(loginTab, registerTab, forgotTab);
        return tabs;
    }

    private Node buildLogin() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        TextField user = new TextField();
        user.setPromptText("帳號 (userId)");

        PasswordField pwd = new PasswordField();
        pwd.setPromptText("密碼");

        Button btn = new Button("登入");
        btn.setOnAction(e -> {
            if (userService.authenticate(user.getText(), pwd.getText())) {
                alert("✅ 登入成功：" + userService.getCurrentUserId());
            } else {
                alert("❌ 登入失敗：帳號或密碼錯誤");
            }
        });

        root.getChildren().addAll(new Label("帳號"), user, new Label("密碼"), pwd, btn);
        return root;
    }

    private Node buildRegister() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        TextField user = new TextField();
        user.setPromptText("帳號 (userId)");

        PasswordField pwd = new PasswordField();
        pwd.setPromptText("密碼 (至少4碼)");

        TextField email = new TextField();
        email.setPromptText("Email");

        TextField phone = new TextField();
        phone.setPromptText("Phone");

        Button btn = new Button("註冊");
        btn.setOnAction(e -> {
            boolean ok = userService.registerUser(user.getText(), pwd.getText(), email.getText(), phone.getText());
            alert(ok ? "✅ 註冊成功" : "❌ 註冊失敗（帳號重複或資料不完整）");
        });

        root.getChildren().addAll(new Label("帳號"), user, new Label("密碼"), pwd, new Label("Email"), email, new Label("Phone"), phone, btn);
        return root;
    }

    private Node buildForgot() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        TextField userOrEmail = new TextField();
        userOrEmail.setPromptText("輸入 userId 或 Email");

        Button send = new Button("取得重設碼");
        Label codeHint = new Label("");
        codeHint.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

        send.setOnAction(e -> {
            String code = userService.requestPasswordReset(userOrEmail.getText());
            if (code == null) {
                alert("❌ 找不到此帳號/Email");
            } else {
                // 示範：直接顯示重設碼（實務上應寄信/簡訊）
                codeHint.setText("重設碼（示範顯示）: " + code);
            }
        });

        TextField code = new TextField();
        code.setPromptText("輸入重設碼");

        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("新密碼 (至少4碼)");

        Button confirm = new Button("確認重設");
        confirm.setOnAction(e -> {
            boolean ok = userService.confirmPasswordReset(userOrEmail.getText(), code.getText(), newPwd.getText());
            alert(ok ? "✅ 密碼已重設" : "❌ 重設失敗（重設碼/新密碼不正確）");
        });

        root.getChildren().addAll(
                new Label("帳號或 Email"), userOrEmail,
                send, codeHint,
                new Label("重設碼"), code,
                new Label("新密碼"), newPwd,
                confirm
        );

        return root;
    }

    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("提示");
        alert.showAndWait();
    }
}
