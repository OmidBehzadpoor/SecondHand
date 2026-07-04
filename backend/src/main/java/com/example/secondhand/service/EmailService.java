package com.example.secondhand.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("تایید ایمیل - سامانه دست دوم");
        message.setText("برای تایید ایمیل خود روی لینک زیر کلیک کنید:\n\n" +
                "http://localhost:8080/api/auth/verify-email?token=" + token);
        mailSender.send(message);
    }
}