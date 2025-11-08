package com.example.room.service.Impl;

import com.example.room.dto.request.BookingEmailRequest;
import com.example.room.dto.request.ContractEmailRequest;
import com.example.room.dto.request.PaymentMonthlyRequest;
import com.example.room.service.EmailService;
import com.example.room.utils.Enums.BookingStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendRegisterSuccessEmail( String email, String fullName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);

        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("email", email);
        String htmlContent = templateEngine.process("register-success",context);

        helper.setTo(fromEmail);
        helper.setSubject(email);
        helper.setText(htmlContent,true);
        mailSender.send(message);
    }

    @Override
    public void sendBooking(BookingEmailRequest request, String email) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Context context = new Context();

        context.setVariable("userName", request.getUserName());

        context.setVariable("bookingId", request.getBookingId());
        context.setVariable("roomName", request.getRoomName());
        context.setVariable("roomAddress", request.getRoomAddress());
        context.setVariable("startDate", request.getStartDate());
        context.setVariable("endDate", request.getEndDate());
        context.setVariable("status", request.getStatus().name());

        // Thông tin chủ trọ
        context.setVariable("ownerName", request.getOwnerName());
        context.setVariable("ownerPhone", request.getOwnerPhone());
        context.setVariable("ownerEmail", request.getOwnerEmail());

        context.setVariable("note",request.getNote());

        if (request.getStatus() == BookingStatus.CONFIRMED){
            context.setVariable("vietQR",request.getLinkQR());
            String htmlContent = templateEngine.process("booking-email", context);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Xác nhận đặt phòng #" + request.getBookingId());
            helper.setText(htmlContent, true);
        }
        if(request.getStatus() == BookingStatus.COMPLETED){
            String htmlContent = templateEngine.process("booking-successfully", context);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Hoàn tất đặt phòng #" + request.getBookingId());
            helper.setText(htmlContent, true);
        }

        mailSender.send(message);
    }

    @Override
    public void sendContractInfoWithAttachment(ContractEmailRequest request, String email, String attachmentPath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String start = request.getStartDate() != null ? request.getStartDate().format(df) : null;
        String end = request.getEndDate() != null ? request.getEndDate().format(df) : null;

        context.setVariable("recipientName", request.getRecipientName());
        context.setVariable("contractId", request.getContractId());
        context.setVariable("startDate", start);
        context.setVariable("endDate", end);

        context.setVariable("roomName", request.getRoomName());
        context.setVariable("roomAddress", request.getRoomAddress());
        context.setVariable("price", request.getPrice());

        context.setVariable("ownerName", request.getOwnerName());
        context.setVariable("ownerEmail", request.getOwnerEmail());
        context.setVariable("ownerPhone", request.getOwnerPhone());

        context.setVariable("renterName", request.getRenterName());
        context.setVariable("renterEmail", request.getRenterEmail());
        context.setVariable("renterPhone", request.getRenterPhone());

        context.setVariable("contractUrl", request.getContractUrl());
        context.setVariable("year", request.getYear());

        String htmlContent = templateEngine.process("contract-infor", context);

        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Thông tin hợp đồng #" + request.getContractId());
        helper.setText(htmlContent, true);

        // Attach file if exists
        if (attachmentPath != null) {
            File file = new File(attachmentPath);
            if (file.exists() && file.isFile()) {
                FileSystemResource resource = new FileSystemResource(file);
                helper.addAttachment(file.getName(), resource);
            }
        }

        mailSender.send(message);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        FileSystemResource file = new FileSystemResource(new File(attachmentPath));
        helper.addAttachment(file.getFilename(), file);

        mailSender.send(message);
    }

    @Override
    public void sendPaymentMonthly(PaymentMonthlyRequest request, String email) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("paymentId", request.getPaymentId());
        context.setVariable("paymentPeriod", request.getPaymentPeriod());
        context.setVariable("roomName", request.getRoomName());
        context.setVariable("roomAddress", request.getRoomAddress());
        context.setVariable("ownerName", request.getOwnerName());
        context.setVariable("ownerPhone", request.getOwnerPhone());
        context.setVariable("ownerEmail", request.getOwnerEmail());

        context.setVariable("baseRent",  request.getBaseRent());
        context.setVariable("services", request.getServices());
        context.setVariable("servicesTotal", request.getServicesTotal());
        context.setVariable("grandTotal",  request.getGrandTotal());

        context.setVariable("vietQR", request.getVietQR());

        String htmlContent = templateEngine.process("payment-monthly", context);

        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Thông báo thanh toán hàng tháng — " + (request.getRoomName() != null ? request.getRoomName() : ""));
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}