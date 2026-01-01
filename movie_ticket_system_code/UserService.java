package org.example.App.services;

import java.security.SecureRandom;
import java.util.*;

/**
 * 使用者服務（示範版）
 * - 登入/登出
 * - 註冊
 * - 修改密碼
 * - 忘記密碼（產生重設碼 -> 驗證 -> 設定新密碼）
 * - 身分證字號驗證（台灣身分證簡化驗證）
 */
public class UserService {

    public static class UserAccount {
        public String userId;
        public String password;
        public String email;
        public String phone;
        public boolean isAdmin;

        // profile
        public String nationalId; // 台灣身分證字號（可選）
        public String area;       // 使用者所在地區（推薦用）

        public UserAccount(String userId, String password, String email, String phone, boolean isAdmin) {
            this.userId = userId;
            this.password = password;
            this.email = email;
            this.phone = phone;
            this.isAdmin = isAdmin;
            this.area = "台北";
        }
    }

    private final Map<String, UserAccount> users = new HashMap<>();
    private String currentUserId;

    /** 忘記密碼：userId/email -> resetCode */
    private final Map<String, String> resetCodes = new HashMap<>();
    private final SecureRandom rnd = new SecureRandom();

    public UserService() {
        // 初始化一個管理員帳號
        users.put("admin", new UserAccount("admin", "admin123", "admin@example.com", "0912345678", true));

        // 初始化一個一般帳號
        users.put("user", new UserAccount("user", "1234", "user@example.com", "0987654321", false));
    }

    public boolean authenticate(String userId, String password) {
        if (userId == null || password == null) return false;
        UserAccount account = users.get(userId);
        if (account != null && account.password.equals(password)) {
            currentUserId = userId;
            return true;
        }
        return false;
    }

    public boolean registerUser(String userId, String password, String email, String phone) {
        if (userId == null || userId.isBlank()) return false;
        if (password == null || password.length() < 4) return false;
        if (users.containsKey(userId)) return false;

        users.put(userId, new UserAccount(userId, password, email, phone, false));
        return true;
    }

    public void logout() {
        currentUserId = null;
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public boolean isCurrentUserAdmin() {
        if (!isLoggedIn()) return false;
        UserAccount account = users.get(currentUserId);
        return account != null && account.isAdmin;
    }

    public Map<String, UserAccount> getAllUsers() {
        // 若希望只允許管理員查看，可在 UI 端再限制。
        return users;
    }

    // =========================
    // Password
    // =========================

    public boolean changePassword(String oldPwd, String newPwd) {
        if (!isLoggedIn()) return false;
        UserAccount account = users.get(currentUserId);
        if (account == null) return false;
        if (!Objects.equals(account.password, oldPwd)) return false;
        if (!isValidPassword(newPwd)) return false;
        account.password = newPwd;
        return true;
    }

    /**
     * 忘記密碼：輸入 userId 或 email 其中之一，回傳 6 碼重設碼（示範：直接回傳給 UI）
     */
    public String requestPasswordReset(String userIdOrEmail) {
        UserAccount account = findByUserIdOrEmail(userIdOrEmail);
        if (account == null) return null;
        String code = String.format("%06d", rnd.nextInt(1_000_000));
        resetCodes.put(account.userId, code);
        return code;
    }

    /** 忘記密碼：驗證重設碼並設定新密碼 */
    public boolean confirmPasswordReset(String userIdOrEmail, String resetCode, String newPwd) {
        UserAccount account = findByUserIdOrEmail(userIdOrEmail);
        if (account == null) return false;
        if (resetCode == null) return false;
        String expect = resetCodes.get(account.userId);
        if (!resetCode.equals(expect)) return false;
        if (!isValidPassword(newPwd)) return false;
        account.password = newPwd;
        resetCodes.remove(account.userId);
        return true;
    }

    private UserAccount findByUserIdOrEmail(String userIdOrEmail) {
        if (userIdOrEmail == null) return null;
        String s = userIdOrEmail.trim();
        if (s.isEmpty()) return null;
        UserAccount byId = users.get(s);
        if (byId != null) return byId;
        for (UserAccount u : users.values()) {
            if (u.email != null && u.email.equalsIgnoreCase(s)) return u;
        }
        return null;
    }

    private boolean isValidPassword(String pwd) {
        return pwd != null && pwd.length() >= 4;
    }

    // =========================
    // Profile
    // =========================

    public boolean setAreaForCurrentUser(String area) {
        if (!isLoggedIn()) return false;
        UserAccount account = users.get(currentUserId);
        if (account == null) return false;
        if (area == null || area.isBlank()) return false;
        account.area = area.trim();
        return true;
    }

    public String getAreaOfCurrentUser() {
        if (!isLoggedIn()) return "台北";
        UserAccount account = users.get(currentUserId);
        return account != null && account.area != null ? account.area : "台北";
    }

    public boolean setNationalIdForCurrentUser(String nationalId) {
        if (!isLoggedIn()) return false;
        if (nationalId == null || nationalId.isBlank()) return false;
        if (!validateTaiwanId(nationalId)) return false;
        UserAccount account = users.get(currentUserId);
        if (account == null) return false;
        account.nationalId = nationalId.toUpperCase();
        return true;
    }

    public String getNationalIdOfCurrentUser() {
        if (!isLoggedIn()) return null;
        UserAccount account = users.get(currentUserId);
        return account == null ? null : account.nationalId;
    }

    // =========================
    // Taiwan ID validation
    // =========================

    /**
     * 台灣身分證字號驗證（一般規則）：
     * - 1 碼英文字母 + 9 碼數字
     * - 第 2 碼為 1/2（男/女），此處不限制也可
     *
     * 公式：
     * letter -> 2 位數 XY
     * sum = X*1 + Y*9 + d1*8 + d2*7 + ... + d8*1 + d9*1
     * sum % 10 == 0
     */
    public boolean validateTaiwanId(String id) {
        if (id == null) return false;
        String s = id.trim().toUpperCase();
        if (!s.matches("^[A-Z][0-9]{9}$")) return false;

        int[] map = letterToCode(s.charAt(0));
        if (map == null) return false;
        int x = map[0];
        int y = map[1];

        int sum = 0;
        sum += x * 1;
        sum += y * 9;

        // digits 1..9
        for (int i = 1; i <= 9; i++) {
            int d = s.charAt(i) - '0';
            int w;
            if (i <= 8) {
                w = 9 - i; // i=1 ->8, i=8 ->1
            } else {
                w = 1; // i=9 check digit
            }
            sum += d * w;
        }

        return sum % 10 == 0;
    }

    private int[] letterToCode(char c) {
        // A=10, B=11, ... Z=35（但 I/O/W 等有跳號），此處用官方對照表
        final String letters = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
        int idx = letters.indexOf(c);
        if (idx < 0) return null;
        int code = idx + 10;
        return new int[]{code / 10, code % 10};
    }
}
