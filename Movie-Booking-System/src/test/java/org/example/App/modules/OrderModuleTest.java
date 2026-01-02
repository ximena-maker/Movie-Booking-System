package org.example.App.modules;

import javafx.stage.Stage;
import org.example.App.MainApp;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class OrderModuleTest extends ApplicationTest {

  @Override
  public void start(Stage stage) {
    new MainApp().start(stage);
  }

  // =========================
  // helper：登入為一般使用者
  // =========================
  private void loginAsUser() {
    clickOn(".text-field").write("user");
    clickOn(".password-field").write("1234");
    clickOn("🔐 登入");
  }

  // =========================
  // Test 1：登入後可進入訂單查詢頁
  // =========================
  @Test
  void order_page_should_be_accessible_after_login() {
    loginAsUser();

    clickOn("📦 訂單查詢");

    verifyThat("📦 我的訂單", isVisible());
  }

  // =========================
  // Test 2：無訂單時顯示提示文字
  // =========================
  @Test
  void order_without_any_booking_should_show_empty_message() {
    loginAsUser();

    clickOn("📦 訂單查詢");

    verifyThat("目前沒有符合條件的訂單", isVisible());
  }

  // =========================
  // Test 3：點擊空訂單提示不會當掉
  // =========================
  @Test
  void order_click_empty_item_should_not_crash() {
    loginAsUser();

    clickOn("📦 訂單查詢");

    clickOn("目前沒有符合條件的訂單");

    // 仍然停留在訂單頁
    verifyThat("📄 訂單詳情", isVisible());
  }

  // =========================
  // Test 4：搜尋欄存在且可輸入
  // =========================
  @Test
  void order_search_field_should_be_visible() {
    loginAsUser();

    clickOn("📦 訂單查詢");

    clickOn(".text-field").write("TEST");

    verifyThat("🔍 查詢", isVisible());
  }
}
