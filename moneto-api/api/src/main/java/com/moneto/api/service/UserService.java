package com.moneto.api.service;

import com.moneto.api.model.Budget;
import com.moneto.api.model.Currency;
import com.moneto.api.model.SavingGoal;
import com.moneto.api.model.SavingGoalStatus;
import com.moneto.api.model.User;
import com.moneto.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moneto.api.model.Alert;
import com.moneto.api.model.AlertType;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BudgetRepository budgetRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final ExchangeRateService exchangeRateService;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final AlertRepository alertRepository;
    private final AuditService auditService;

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

        if (currency != null) {
            Currency oldCurrency = user.getCurrency();
            Currency newCurrency = Currency.valueOf(currency);

            if (oldCurrency != newCurrency) {
                convertActiveData(user, oldCurrency, newCurrency);
                user.setCurrency(newCurrency);
            }
        }

        if (monthStart != null) user.setMonthStart(monthStart);
        if (budgetAlerts != null) user.setBudgetAlerts(budgetAlerts);
        if (recurringReminders != null) user.setRecurringReminders(recurringReminders);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();
        Long userId = user.getId();

        auditService.log(user.getEmail(), "DELETE_ACCOUNT", null);

        transactionRepository.deleteAll(transactionRepository.findByUserId(userId));
        budgetRepository.deleteAll(budgetRepository.findByUserId(userId));
        recurringTransactionRepository.deleteAll(recurringTransactionRepository.findByUserId(userId));
        savingGoalRepository.deleteAll(savingGoalRepository.findByUserId(userId));
        notificationRepository.deleteAll(notificationRepository.findByUserId(userId));
        categoryRepository.deleteAll(categoryRepository.findByUserId(userId));

        userRepository.delete(user);
    }

    private void convertActiveData(User user, Currency from, Currency to) {
        List<Budget> budgets = budgetRepository.findByUserId(user.getId());
        for (Budget budget : budgets) {
            BigDecimal converted = exchangeRateService.convert(
                    budget.getAmount(), from.name(), to.name());
            budget.setAmount(converted);
            budgetRepository.save(budget);
        }

        List<SavingGoal> goals = savingGoalRepository
                .findByUserIdAndStatus(user.getId(), SavingGoalStatus.IN_PROGRESS);
        for (SavingGoal goal : goals) {
            BigDecimal convertedTarget = exchangeRateService.convert(
                    goal.getTargetAmount(), from.name(), to.name());
            BigDecimal convertedCurrent = exchangeRateService.convert(
                    goal.getCurrentAmount(), from.name(), to.name());
            goal.setTargetAmount(convertedTarget);
            goal.setCurrentAmount(convertedCurrent);
            savingGoalRepository.save(goal);
        }

        // Konverto pragjet e alerteve aktive
        List<Alert> alerts = alertRepository.findByUserIdAndActiveTrue(user.getId());
        for (Alert alert : alerts) {
            // Alerti i bilancit negativ s'ka prag për të konvertuar
            if (alert.getType() == AlertType.NEGATIVE_BALANCE) continue;

            BigDecimal converted = exchangeRateService.convert(
                    alert.getThreshold(), from.name(), to.name());
            alert.setThreshold(converted);
            alertRepository.save(alert);
        }
    }
}