package com.cinebook.module.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.verify-base-url}")
    private String verifyBaseUrl;

    public void sendVerifyEmail(String toEmail, String userName, String verifyToken) {
        String link = verifyBaseUrl + "?token=" + verifyToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("CineBook - Verify your email");
        message.setText("""
                Hi %s,

                Please click the link provided to verify account (Expired after 30 minutes):
                %s

                If you do not register for CineBook, please ignore this email.
                """.formatted(userName, link));

        mailSender.send(message);
    }
}
