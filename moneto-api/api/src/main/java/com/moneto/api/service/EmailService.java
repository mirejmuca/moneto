package com.moneto.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Moneto verification code");
        message.setText(
                "Welcome to Moneto!\n\n" +
                        "Your verification code is: " + code + "\n\n" +
                        "Enter this code in the app to verify your account.\n\n" +
                        "If you did not create this account, you can ignore this email."
        );
        message.setFrom("moneto.theapp@gmail.com");

        mailSender.send(message);
    }
}