package com.moneto.api.dto;

import com.moneto.api.model.Currency;
import com.moneto.api.model.Role;
import com.moneto.api.model.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Currency currency;
    private Integer monthStart;
    private Boolean budgetAlerts;
    private Boolean recurringReminders;
    private Boolean isVerified;
    private Role role;
    private SubscriptionTier subscriptionTier;
    private LocalDate subscriptionExpiresAt;
}