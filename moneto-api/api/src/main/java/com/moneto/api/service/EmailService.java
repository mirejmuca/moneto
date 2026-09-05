package com.moneto.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

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
        message.setFrom(fromEmail);

        mailSender.send(message);
    }

    public void sendInvoiceEmail(String to, String name, String plan, String amount,
                                 LocalDate paymentDate, LocalDate expiryDate,
                                 String transactionId, String cardLast4) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Moneto subscription invoice");
        message.setText(
                "Hi " + name + ",\n\n" +
                        "Thank you for subscribing to Moneto " + plan + "!\n\n" +
                        "─────────────────────────────\n" +
                        "  INVOICE\n" +
                        "─────────────────────────────\n" +
                        "  Plan:            " + plan + "\n" +
                        "  Amount:          " + amount + "\n" +
                        "  Payment date:    " + paymentDate + "\n" +
                        "  Renewal date:    " + expiryDate + "\n" +
                        "  Card:            **** **** **** " + cardLast4 + "\n" +
                        "  Transaction ID:  " + transactionId + "\n" +
                        "─────────────────────────────\n\n" +
                        "Your subscription is now active. Enjoy your premium features!\n\n" +
                        "If you have any questions, just reply to this email.\n\n" +
                        "— The Moneto Team"
        );
        message.setFrom(fromEmail);

        mailSender.send(message);
    }
}