package com.example.room.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendRegisterSuccessEmail(String email, String fullName) throws MessagingException;
}
