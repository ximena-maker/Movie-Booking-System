package org.example.service;

import java.util.Map;

public class IDValidator {
    // 台灣身分證字號加權驗證（常見作業會用到）
    private static final Map<Character, Integer> CITY = Map.ofEntries(
            Map.entry('A', 10), Map.entry('B', 11), Map.entry('C', 12), Map.entry('D', 13),
            Map.entry('E', 14), Map.entry('F', 15), Map.entry('G', 16), Map.entry('H', 17),
            Map.entry('I', 34), Map.entry('J', 18), Map.entry('K', 19), Map.entry('L', 20),
            Map.entry('M', 21), Map.entry('N', 22), Map.entry('O', 35), Map.entry('P', 23),
            Map.entry('Q', 24), Map.entry('R', 25), Map.entry('S', 26), Map.entry('T', 27),
            Map.entry('U', 28), Map.entry('V', 29), Map.entry('W', 32), Map.entry('X', 30),
            Map.entry('Y', 31), Map.entry('Z', 33)
    );

    public boolean isValidTWId(String id) {
        if (id == null) return false;
        id = id.trim().toUpperCase();
        if (!id.matches("^[A-Z][12][0-9]{8}$")) return false;

        int code = CITY.getOrDefault(id.charAt(0), -1);
        if (code < 0) return false;

        int n1 = code / 10;
        int n2 = code % 10;

        int sum = n1 * 1 + n2 * 9;
        int[] w = {8,7,6,5,4,3,2,1,1};
        for (int i = 1; i <= 9; i++) {
            int d = id.charAt(i) - '0';
            sum += d * w[i-1];
        }
        return sum % 10 == 0;
    }
}
