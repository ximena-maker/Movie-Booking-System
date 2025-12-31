package org.example.service;

import org.example.model.PaymentResult;

import java.time.YearMonth;

public class PaymentService {
    // 交易限時（毫秒）：例如 90 秒
    public static final long TIMEOUT_MS = 90_000;

    public PaymentResult validateAndPay(String cardNo, String expYYYYMM, String cvv, long createdAtMillis) {
        long now = System.currentTimeMillis();
        if (now - createdAtMillis > TIMEOUT_MS) {
            return new PaymentResult(false, "交易逾時，請重新下單");
        }
        if (!isValidCardLuhn(cardNo)) return new PaymentResult(false, "信用卡號驗證失敗");
        if (!isValidExp(expYYYYMM)) return new PaymentResult(false, "到期日格式或時間不合法");
        if (cvv == null || !cvv.matches("^\\d{3}$")) return new PaymentResult(false, "CVV 不合法");
        return new PaymentResult(true, "付款成功");
    }

    private boolean isValidExp(String exp) {
        if (exp == null || !exp.matches("^\\d{4}-\\d{2}$")) return false;
        YearMonth ym = YearMonth.parse(exp);
        return !ym.isBefore(YearMonth.now());
    }

    // Luhn 演算法
    public boolean isValidCardLuhn(String s) {
        if (s == null) return false;
        String digits = s.replaceAll("\\s+", "");
        if (!digits.matches("^\\d{13,19}$")) return false;

        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }
}

