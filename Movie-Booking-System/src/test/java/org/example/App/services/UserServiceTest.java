package org.example.App.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

  private UserService userService;

  @BeforeEach
  void setup() {
    userService = new UserService();
  }

  // =========================
  // Authentication
  // =========================

  @Test
  void authenticate_success_shouldLogin() {
    boolean ok = userService.authenticate("user", "1234");
    assertTrue(ok);
    assertTrue(userService.isLoggedIn());
    assertEquals("user", userService.getCurrentUserId());
  }

  @Test
  void authenticate_wrongPassword_shouldFail() {
    assertFalse(userService.authenticate("user", "wrong"));
    assertFalse(userService.isLoggedIn());
  }

  @Test
  void authenticate_nullInput_shouldFail() {
    assertFalse(userService.authenticate(null, null));
  }

  @Test
  void logout_shouldClearLoginState() {
    userService.authenticate("user", "1234");
    userService.logout();
    assertFalse(userService.isLoggedIn());
  }

  // =========================
  // Register
  // =========================

  @Test
  void registerUser_success() {
    boolean ok = userService.registerUser(
        "newUser", "abcd", "a@b.com", "0911222333"
    );
    assertTrue(ok);
  }

  @Test
  void registerUser_duplicateUser_shouldFail() {
    assertFalse(userService.registerUser(
        "user", "abcd", "x@y.com", "0911222333"
    ));
  }

  @Test
  void registerUser_shortPassword_shouldFail() {
    assertFalse(userService.registerUser(
        "u2", "123", "x@y.com", "0911222333"
    ));
  }

  @Test
  void registerUser_blankUserId_shouldFail() {
    assertFalse(userService.registerUser(
        "", "abcd", "x@y.com", "0911222333"
    ));
  }

  // =========================
  // Admin
  // =========================

  @Test
  void isCurrentUserAdmin_adminShouldBeTrue() {
    userService.authenticate("admin", "admin123");
    assertTrue(userService.isCurrentUserAdmin());
  }

  @Test
  void isCurrentUserAdmin_userShouldBeFalse() {
    userService.authenticate("user", "1234");
    assertFalse(userService.isCurrentUserAdmin());
  }

  @Test
  void isCurrentUserAdmin_notLogin_shouldBeFalse() {
    assertFalse(userService.isCurrentUserAdmin());
  }

  // =========================
  // Change password
  // =========================

  @Test
  void changePassword_success() {
    userService.authenticate("user", "1234");
    boolean ok = userService.changePassword("1234", "newpass");
    assertTrue(ok);
  }

  @Test
  void changePassword_wrongOldPassword_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.changePassword("wrong", "newpass"));
  }

  @Test
  void changePassword_notLogin_shouldFail() {
    assertFalse(userService.changePassword("1234", "newpass"));
  }

  @Test
  void changePassword_invalidNewPassword_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.changePassword("1234", "123"));
  }

  // =========================
  // Forgot password
  // =========================

  @Test
  void requestPasswordReset_byUserId_shouldReturnCode() {
    String code = userService.requestPasswordReset("user");
    assertNotNull(code);
    assertEquals(6, code.length());
  }

  @Test
  void requestPasswordReset_byEmail_shouldReturnCode() {
    String code = userService.requestPasswordReset("user@example.com");
    assertNotNull(code);
  }

  @Test
  void requestPasswordReset_invalidUser_shouldReturnNull() {
    assertNull(userService.requestPasswordReset("notExist"));
  }

  @Test
  void confirmPasswordReset_success() {
    String code = userService.requestPasswordReset("user");
    boolean ok = userService.confirmPasswordReset(
        "user", code, "newpass"
    );
    assertTrue(ok);
  }

  @Test
  void confirmPasswordReset_wrongCode_shouldFail() {
    userService.requestPasswordReset("user");
    assertFalse(userService.confirmPasswordReset(
        "user", "000000", "newpass"
    ));
  }

  @Test
  void confirmPasswordReset_invalidPassword_shouldFail() {
    String code = userService.requestPasswordReset("user");
    assertFalse(userService.confirmPasswordReset(
        "user", code, "123"
    ));
  }

  @Test
  void confirmPasswordReset_unknownUser_shouldFail() {
    assertFalse(userService.confirmPasswordReset(
        "unknown", "123456", "abcd"
    ));
  }

  // =========================
  // Profile
  // =========================

  @Test
  void setAreaForCurrentUser_success() {
    userService.authenticate("user", "1234");
    assertTrue(userService.setAreaForCurrentUser("新北"));
    assertEquals("新北", userService.getAreaOfCurrentUser());
  }

  @Test
  void setAreaForCurrentUser_notLogin_shouldFail() {
    assertFalse(userService.setAreaForCurrentUser("台中"));
  }

  @Test
  void getAreaOfCurrentUser_notLogin_shouldReturnDefault() {
    assertEquals("台北", userService.getAreaOfCurrentUser());
  }

  @Test
  void setNationalIdForCurrentUser_success() {
    userService.authenticate("user", "1234");
    assertTrue(userService.setNationalIdForCurrentUser("A123456789"));
    assertEquals("A123456789", userService.getNationalIdOfCurrentUser());
  }

  @Test
  void setNationalIdForCurrentUser_invalidId_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.setNationalIdForCurrentUser("A123"));
  }

  // =========================
  // Taiwan ID validation
  // =========================

  @Test
  void validateTaiwanId_valid_shouldPass() {
    assertTrue(userService.validateTaiwanId("A123456789"));
  }

  @Test
  void validateTaiwanId_invalidFormat_shouldFail() {
    assertFalse(userService.validateTaiwanId("123456789"));
  }

  @Test
  void validateTaiwanId_invalidChecksum_shouldFail() {
    assertFalse(userService.validateTaiwanId("A123456788"));
  }

  @Test
  void validateTaiwanId_null_shouldFail() {
    assertFalse(userService.validateTaiwanId(null));
  }

  @Test
  void getAllUsers_shouldReturnUsersMap() {
    Map<String, UserService.UserAccount> users = userService.getAllUsers();
    assertNotNull(users);
    assertTrue(users.containsKey("admin"));
    assertTrue(users.containsKey("user"));
  }

  @Test
  void authenticate_nullUserId_shouldFail() {
    assertFalse(userService.authenticate(null, "1234"));
  }

  @Test
  void authenticate_nullPassword_shouldFail() {
    assertFalse(userService.authenticate("user", null));
  }

  @Test
  void registerUser_nullUserId_shouldFail() {
    assertFalse(userService.registerUser(null, "abcd", "a@b.com", "0912"));
  }

  @Test
  void registerUser_nullPassword_shouldFail() {
    assertFalse(userService.registerUser("u123", null, "a@b.com", "0912"));
  }

  @Test
  void changePassword_notLoggedIn_shouldFail() {
    assertFalse(userService.changePassword("1234", "newpass"));
  }

  @Test
  void requestPasswordReset_nullInput_shouldReturnNull() {
    assertNull(userService.requestPasswordReset(null));
  }

  @Test
  void requestPasswordReset_blankInput_shouldReturnNull() {
    assertNull(userService.requestPasswordReset("   "));
  }

  @Test
  void confirmPasswordReset_nullResetCode_shouldFail() {
    userService.requestPasswordReset("user");
    assertFalse(userService.confirmPasswordReset("user", null, "newpass"));
  }

  @Test
  void confirmPasswordReset_invalidNewPassword_shouldFail() {
    String code = userService.requestPasswordReset("user");
    assertFalse(userService.confirmPasswordReset("user", code, "123"));
  }

  @Test
  void requestPasswordReset_byEmail_shouldFindUser() {
    String code = userService.requestPasswordReset("user@example.com");
    assertNotNull(code);
  }

  @Test
  void setArea_nullArea_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.setAreaForCurrentUser(null));
  }

  @Test
  void setArea_blankArea_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.setAreaForCurrentUser(" "));
  }

  @Test
  void setNationalId_null_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.setNationalIdForCurrentUser(null));
  }

  @Test
  void setNationalId_invalid_shouldFail() {
    userService.authenticate("user", "1234");
    assertFalse(userService.setNationalIdForCurrentUser("A123"));
  }

  @Test
  void validateTaiwanId_invalidLetter_shouldFail() {
    assertFalse(userService.validateTaiwanId("Z123456789"));
  }

  @Test
  void confirmPasswordReset_byEmail_shouldHitEmailBranch() {
    String code = userService.requestPasswordReset("user@example.com");
    assertNotNull(code);

    boolean ok = userService.confirmPasswordReset(
        "user@example.com", code, "newpass"
    );

    assertTrue(ok);
  }

  @Test
  void confirmPasswordReset_nullNewPassword_shouldFail() {
    String code = userService.requestPasswordReset("user");
    assertFalse(userService.confirmPasswordReset("user", code, null));
  }

  @Test
  void setArea_accountMissing_shouldFail() {
    userService.authenticate("user", "1234");

    // 破壞內部狀態（只為 coverage）
    userService.getAllUsers().remove("user");

    assertFalse(userService.setAreaForCurrentUser("台中"));
  }

  @Test
  void getArea_accountMissing_shouldReturnDefault() {
    userService.authenticate("user", "1234");
    userService.getAllUsers().remove("user");

    assertEquals("台北", userService.getAreaOfCurrentUser());
  }

  @Test
  void setNationalId_accountMissing_shouldFail() {
    userService.authenticate("user", "1234");
    userService.getAllUsers().remove("user");

    assertFalse(userService.setNationalIdForCurrentUser("A123456789"));
  }

  @Test
  void getNationalId_accountMissing_shouldReturnNull() {
    userService.authenticate("user", "1234");
    userService.getAllUsers().remove("user");

    assertNull(userService.getNationalIdOfCurrentUser());
  }


}
