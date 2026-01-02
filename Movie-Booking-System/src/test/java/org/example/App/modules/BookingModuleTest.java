package org.example.App.modules;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import org.example.App.services.BookingService;
import org.example.App.services.PriceService;
import org.example.App.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class BookingModuleTest extends ApplicationTest {

  private BookingService bookingService;
  private PriceService priceService;
  private UserService userService;

  @Override
  public void start(Stage stage) {
    bookingService = new BookingService();
    priceService = new PriceService();
    userService = new UserService(); // 預設未登入

    BookingModule module = new BookingModule(bookingService, priceService, userService);

    // 直接測 BookingModule，不走 MainApp / 側邊欄
    Scene scene = new Scene((ScrollPane) module.build(), 1100, 900);
    stage.setScene(scene);
    stage.show();
  }

  @AfterEach
  void cleanupDialogs() {
    // 若有 Alert/Dialog 沒關，順手關掉避免影響下一個測試
    tryClick("OK");
    tryClick("確定");
    tryClick("Close");
    tryClick("取消");
  }

  @Test
  void submitWithoutLogin_shouldShowSomeDialog_andNotCrash() {
    // 先把 ScrollPane 拉到底，確保「✅ 確認訂票」可見
    scrollToBottom();

    // 點擊「✅ 確認訂票」
    clickOnVisibleButtonText("✅ 確認訂票");

    // 不硬抓文字（避免你之後改字就爆），只驗證「有 dialog 出現」
    verifyThat(".dialog-pane", isVisible());

    // 關掉 dialog
    tryClick("OK");
    tryClick("確定");
  }

  @Test
  void openSeatDialog_thenCancel_shouldNotCrash() {
    // 「🪑 選擇座位」通常在中間，但保險起見先稍微往下
    scrollToMiddle();

    clickOnVisibleButtonText("🪑 選擇座位");

    // 座位選擇是 Dialog
    verifyThat(".dialog-pane", isVisible());

    // 關掉（Dialog 有 Cancel）
    tryClick("取消");
    tryClick("Cancel");
  }

  // -------------------------
  // helpers
  // -------------------------

  private void scrollToBottom() {
    ScrollPane sp = lookup(".scroll-pane").queryAs(ScrollPane.class);
    interact(() -> sp.setVvalue(1.0));
    waitForFxEvents();
  }

  private void scrollToMiddle() {
    ScrollPane sp = lookup(".scroll-pane").queryAs(ScrollPane.class);
    interact(() -> sp.setVvalue(0.5));
    waitForFxEvents();
  }

  /**
   * 避免 "returned 2 nodes" / "not visible"：
   * 只點「目前可見」的那顆按鈕。
   */
  private void clickOnVisibleButtonText(String text) {
    Button target = lookup((Button b) ->
        text.equals(b.getText()) && b.isVisible() && !b.isDisabled()
    ).queryAs(Button.class);

    // 有時候剛 setVvalue 還沒 layout 完，等一下事件
    waitForFxEvents();

    clickOn(target);
    waitForFxEvents();
  }

  private void tryClick(String buttonText) {
    try {
      // DialogPane 的按鈕通常是 Button
      DialogPane dp = lookup(".dialog-pane").tryQueryAs(DialogPane.class).orElse(null);
      if (dp == null) return;

      // 直接用文字找
      Button b = lookup((Button btn) ->
          buttonText.equals(btn.getText()) && btn.isVisible()
      ).tryQueryAs(Button.class).orElse(null);

      if (b != null) {
        clickOn(b);
        waitForFxEvents();
      }
    } catch (Exception ignored) {
    }
  }
}
