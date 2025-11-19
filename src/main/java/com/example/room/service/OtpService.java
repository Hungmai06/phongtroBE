package com.example.room.service;

import com.example.room.dto.BaseResponse;
import jakarta.mail.MessagingException;

public interface OtpService {
    void generateAndSendOtp(String email);
    void validateOtp(String email, String otp);
}