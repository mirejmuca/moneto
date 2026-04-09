package com.moneto.api.service;

import com.moneto.api.model.User;
import com.moneto.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String name, String email) {
        User user = getCurrentUser();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User updateSettings(String currency, Integer monthStart,
                               Boolean budgetAlerts, Boolean recurringReminders) {
        User user = getCurrentUser();
        if (currency != null) user.setCurrency(com.moneto.api.model.Currency.valueOf(currency));
        if (monthStart != null) user.setMonthStart(monthStart);
        if (budgetAlerts != null) user.setBudgetAlerts(budgetAlerts);
        if (recurringReminders != null) user.setRecurringReminders(recurringReminders);
        return userRepository.save(user);
    }
}