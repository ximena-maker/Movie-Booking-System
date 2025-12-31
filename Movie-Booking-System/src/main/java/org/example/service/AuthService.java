package org.example.service;

/**
 * AuthService - 使用者認證服務
 * 作用: 處理使用者登入、密碼管理等認證相關功能
 * 功能: 登入驗證、修改密碼、忘記密碼處理
 */

import org.example.data.DataStore;
import org.example.model.User;

import java.security.SecureRandom;
import java.util.Optional;

public class AuthService {
    private final DataStore db; // 資料庫實例
    private final SecureRandom rnd = new SecureRandom(); // 安全隨機數產生器

    public AuthService(DataStore db) { this.db = db; } // 建構子

    /**
     * login - 使用者登入驗證
     * @param username 使用者名稱
     * @param password 密碼（明文）
     * @return Optional<User> 登入成功返回 User 物件，失敗返回 empty
     */
    public Optional<User> login(String username, String password) {
        return db.users.values().stream()
                .filter(u -> u.username().equals(username))
                .filter(u -> u.passwordHash().equals(hash(password)))
                .findFirst();
    }

    /**
     * changePassword - 修改密碼
     * @param username 使用者名稱
     * @param oldPwd 舊密碼
     * @param newPwd 新密碼
     * @return boolean 修改成功返回true，失敗返回false
     */
    public boolean changePassword(String username, String oldPwd, String newPwd) {
        for (var e : db.users.entrySet()) {
            User u = e.getValue();
            if (!u.username().equals(username)) continue;
            if (!u.passwordHash().equals(hash(oldPwd))) return false;
            db.users.put(e.getKey(), new User(u.userId(), u.username(), hash(newPwd)));
            return true;
        }
        return false;
    }

    // 示範：忘記密碼直接重設成隨機 8 碼（正式版應寄信/簡訊/OTP）
    /**
     * forgotPassword - 忘記密碼處理
     * 功能: 為使用者產生一個新的隨機密碼（示範用，正式版應該透過郵件或簡訊發送）
     * @param username 使用者名稱
     * @return String 新密碼，如果使用者不存在則返回 null
     */
    public String forgotPassword(String username) {
        for (var e : db.users.entrySet()) {
            User u = e.getValue();
            if (!u.username().equals(username)) continue;

            String newPwd = randomPwd(8);
            db.users.put(e.getKey(), new User(u.userId(), u.username(), hash(newPwd)));
            return newPwd;
        }
        return null;
    }

    /**
     * randomPwd - 產生隨機密碼
     * @param n 密碼長度
     * @return String 隨機密碼字串
     */
    private String randomPwd(int n) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private String hash(String s) {
        // Demo：用 hashCode 代替（作業若要更像真實可改成 SHA-256 + salt）
        return Integer.toString(s.hashCode());
        /**
     * hash - 密碼雜湊處理
     * 註: 此為示範用，實際專案應使用 SHA-256 或更安全的雜湊演算法加上 salt
     * @param s 明文密碼
     * @return String 雜湊值
     */
}
}

