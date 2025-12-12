package com.example.room.service;

import com.example.room.dto.request.BookingEmailRequest;
import com.example.room.dto.request.ContractEmailRequest;
import com.example.room.dto.request.PaymentMonthlyRequest;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendRegisterSuccessEmail(String email, String fullName,String description) throws MessagingException;
    void sendBooking(BookingEmailRequest request, String email) throws MessagingException;
    void sendContractInfoWithAttachment(ContractEmailRequest request, String email, String attachmentPath) throws MessagingException;
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException;
    void sendPaymentMonthly(PaymentMonthlyRequest request, String email) throws MessagingException;
    void sendHelp(String fullName,String email, String description) throws MessagingException;
}