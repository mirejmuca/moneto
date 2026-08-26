package com.moneto.api.service;

import com.moneto.api.model.*;
import com.moneto.api.repository.AlertRepository;
import com.moneto.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    public List<Alert> getAlerts() {
        subscriptionService.requireTier(SubscriptionTier.PREMIUM);
        User user = userService.getCurrentUser();
        return alertRepository.findByUserId(user.getId());
    }

    public Alert createAlert(AlertType type, Long categoryId, BigDecimal threshold) {
        subscriptionService.requireTier(SubscriptionTier.PREMIUM);
        User user = userService.getCurrentUser();

        // Alertet me prag duhet të kenë një vlerë pozitive
        if (type != AlertType.NEGATIVE_BALANCE) {
            if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Threshold must be greater than zero");
            }
        }

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setType(type);
        alert.setThreshold(threshold);
        alert.setActive(true);

        if (type == AlertType.CATEGORY_SPENDING && categoryId != null) {
            Category category = new Category();
            category.setId(categoryId);
            alert.setCategory(category);
        }

        return alertRepository.save(alert);
    }

    public void deleteAlert(Long alertId) {
        subscriptionService.requireTier(SubscriptionTier.PREMIUM);
        alertRepository.deleteById(alertId);
    }

    // Thirret kur shtohet një transaksion — kontrollon nëse ndonjë alert plotësohet
    public void checkAlerts(User user, Transaction transaction) {
        if (transaction.getType() != TransactionType.EXPENSE) return;
        if (subscriptionService.getEffectiveTier(user) != SubscriptionTier.PREMIUM) return;

        List<Alert> alerts = alertRepository.findByUserIdAndActiveTrue(user.getId());
        String baseCurrency = user.getCurrency().name();

        for (Alert alert : alerts) {
            switch (alert.getType()) {
                case DAILY_SPENDING -> checkDailySpending(user, alert, baseCurrency);
                case CATEGORY_SPENDING -> checkCategorySpending(user, alert, transaction, baseCurrency);
                case MONTHLY_SPENDING -> checkMonthlySpending(user, alert, baseCurrency);
                case LARGE_TRANSACTION -> checkLargeTransaction(user, alert, transaction, baseCurrency);
                case NEGATIVE_BALANCE -> checkNegativeBalance(user, baseCurrency);
            }
        }
    }

    private void checkDailySpending(User user, Alert alert, String baseCurrency) {
        LocalDate today = LocalDate.now();
        List<Transaction> todayTransactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), today, today);

        double todayTotal = todayTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        if (todayTotal > alert.getThreshold().doubleValue()) {
            notificationService.createNotification(user,
                    "Daily spending alert: you've spent over " + alert.getThreshold() + " " + baseCurrency + " today.");
        }
    }

    private void checkCategorySpending(User user, Alert alert, Transaction transaction, String baseCurrency) {
        if (alert.getCategory() == null || transaction.getCategory() == null) return;
        // Kontrollo vetëm nëse transaksioni është i së njëjtës kategori si alerti
        if (!alert.getCategory().getId().equals(transaction.getCategory().getId())) return;

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), monthStart, now);

        double categoryTotal = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(alert.getCategory().getId()))
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        if (categoryTotal > alert.getThreshold().doubleValue()) {
            notificationService.createNotification(user,
                    "Category spending alert: you've spent over " + alert.getThreshold() + " " + baseCurrency +
                            " on " + transaction.getCategory().getName() + " this month.");
        }
    }

    private void checkMonthlySpending(User user, Alert alert, String baseCurrency) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), monthStart, now);

        double monthTotal = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        if (monthTotal > alert.getThreshold().doubleValue()) {
            notificationService.createNotification(user,
                    "Monthly spending alert: you've spent over " + alert.getThreshold() + " " + baseCurrency + " this month.");
        }
    }

    private void checkLargeTransaction(User user, Alert alert, Transaction transaction, String baseCurrency) {
        double amount = exchangeRateService.convert(
                transaction.getAmount(), transaction.getCurrency().name(), baseCurrency).doubleValue();

        if (amount > alert.getThreshold().doubleValue()) {
            notificationService.createNotification(user,
                    "Large transaction alert: you made a payment over " + alert.getThreshold() + " " + baseCurrency + ".");
        }
    }

    private void checkNegativeBalance(User user, String baseCurrency) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), monthStart, now);

        double income = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double expense = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        if (expense > income) {
            notificationService.createNotification(user,
                    "Negative balance alert: your expenses have exceeded your income this month.");
        }
    }


}