package com.example.room.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Component
public class BankAccountUtils {
    public String generateVietQR(String bankCode, String accountNo, String accountName, BigDecimal amount, String addInfo) {
        try {
            String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
            String encodedAddInfo = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);

            String url = String.format(
                    "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankCode, accountNo, amount, encodedAddInfo, encodedAccountName
            );

            return url;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo QR: " + e.getMessage());
        }
    }
}


