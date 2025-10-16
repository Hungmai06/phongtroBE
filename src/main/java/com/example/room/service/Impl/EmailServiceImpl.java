package com.example.room.service.Impl;

import com.example.room.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
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
}