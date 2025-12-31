package org.example.service;

import org.example.data.DataStore;
import org.example.model.User;

import java.security.SecureRandom;
import java.util.Optional;

public class AuthService {
    private final DataStore db;
    private final SecureRandom rnd = new SecureRandom();

    public AuthService(DataStore db) { this.db = db; }

    public Optional<User> login(String username, String password) {
        return db.users.values().stream()
                .filter(u -> u.username().equals(username))
                .filter(u -> u.passwordHash().equals(hash(password)))
                .findFirst();
    }

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

    private String randomPwd(int n) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private String hash(String s) {
        // Demo：用 hashCode 代替（作業若要更像真實可改成 SHA-256 + salt）
        return Integer.toString(s.hashCode());
    }
}
