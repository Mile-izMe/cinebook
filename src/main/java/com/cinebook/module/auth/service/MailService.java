package com.cinebook.module.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.verify-base-url}")
    private String verifyBaseUrl;

    public void sendVerifyEmail(String toEmail, String userName, String verifyToken) {
        String link = verifyBaseUrl + "?token=" + verifyToken;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("CineBook - Verify your account");

            String htmlMsg = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;">
                        <div style="background-color: #e50914; padding: 20px; text-align: center;">
                            <h2 style="color: #ffffff; margin: 0;">CineBook</h2>
                        </div>
                        <div style="padding: 30px; background-color: #ffffff; color: #333333;">
                            <h3 style="margin-top: 0;">Xin chào %s,</h3>
                            <p style="line-height: 1.6;">Thank you for registering at CineBook. Please click the link below to verify your email. This link will be expired in 30 minutes.</p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #e50914; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">Verify Email</a>
                            </div>
                            <p style="font-size: 13px; color: #777777;">If the button does not work, you can copy the link and paste:<br> <a href="%s">%s</a></p>
                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 20px 0;">
                            <p style="font-size: 12px; color: #999999; margin: 0;">If you do not register any account, please ignore this email.</p>
                        </div>
                    </div>
                    """.formatted(userName, link, link, link);

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Error when sending to email: {}", toEmail, e);
        }
    }
}
