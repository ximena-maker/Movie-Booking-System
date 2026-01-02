package org.example.App.modules;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class MainAppTest extends ApplicationTest {

  @Override
  public void start(Stage stage) {
    new org.example.App.MainApp().start(stage);
  }

  @Test
  void login_success_shouldEnterMainPage() {
    // 1) 找到第一個 TextField（userField）
    TextField userField = lookup(".text-field").queryAs(TextField.class);
    clickOn(userField).write("user");

    // 2) 找到 PasswordField（passField）
    PasswordField passField = lookup(".password-field").queryAs(PasswordField.class);
    clickOn(passField).write("1234");

    // 3) 點登入按鈕（用按鈕文字找）
    clickOn("🔐 登入");

    // 4) 驗證進到主畫面：側邊欄的「🎟️ 訂票」應該看得到
    verifyThat("🎟️ 訂票", isVisible());
  }

  @Test
  void login_fail_shouldShowErrorAlert() {
    TextField userField = lookup(".text-field").queryAs(TextField.class);
    clickOn(userField).write("user");

    PasswordField passField = lookup(".password-field").queryAs(PasswordField.class);
    clickOn(passField).write("wrong");

    clickOn("🔐 登入");

    // 你登入失敗會跳 Alert：title「❌ 登入失敗」content「帳號或密碼錯誤」
    // TestFX 可以直接驗證文字是否出現
    verifyThat("帳號或密碼錯誤", isVisible());
  }
}
