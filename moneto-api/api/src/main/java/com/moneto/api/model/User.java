package com.moneto.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency = Currency.USD;

    @Column(name = "budget_alerts")
    private Boolean budgetAlerts = true;

    @Column(name = "recurring_reminders")
    private Boolean recurringReminders = true;

    @Column(name = "month_start")
    private Integer monthStart = 1;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", nullable = false)
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Column(name = "subscription_expires_at")
    private LocalDate subscriptionExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;
}
