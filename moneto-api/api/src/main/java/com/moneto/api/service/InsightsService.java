package com.moneto.api.service;

import com.moneto.api.model.Transaction;
import com.moneto.api.model.TransactionType;
import com.moneto.api.model.User;
import com.moneto.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    public Map<String, Object> getInsights() {
        subscriptionService.requireTier(com.moneto.api.model.SubscriptionTier.PLUS);

        User user = userService.getCurrentUser();
        String baseCurrency = user.getCurrency().name();

        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        int dayOfMonth = now.getDayOfMonth();

        // Muaji i kaluar deri në TË NJËJTËN ditë (krahasim pro-rata, "mollë me mollë").
        // Kështu një muaj i papërfunduar s'krahasohet gabimisht me një muaj të plotë.
        LocalDate lastMonthStart = thisMonthStart.minusMonths(1);
        // Kufizo te e njëjta ditë, por jo më shumë se ditët që ka muaji i kaluar
        int lastMonthLength = lastMonthStart.lengthOfMonth();
        int compareDay = Math.min(dayOfMonth, lastMonthLength);
        LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(compareDay);

        List<Transaction> thisMonth = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), thisMonthStart, now);
        List<Transaction> lastMonth = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), lastMonthStart, lastMonthEnd);

        double thisMonthExpense = sumExpenses(thisMonth, baseCurrency);
        double lastMonthExpense = sumExpenses(lastMonth, baseCurrency);
        double thisMonthIncome = sumIncome(thisMonth, baseCurrency);

        Map<String, Object> result = new HashMap<>();

        // 1. Krahasim muaj-me-muaj
        if (lastMonthExpense > 0) {
            double change = ((thisMonthExpense - lastMonthExpense) / lastMonthExpense) * 100;
            result.put("monthOverMonthChange", Math.round(change));
        } else {
            result.put("monthOverMonthChange", null);
        }
        result.put("thisMonthExpense", Math.round(thisMonthExpense));
        result.put("lastMonthExpense", Math.round(lastMonthExpense));

        // 2. Kategoria në rritje
        result.put("topGrowingCategory", findTopGrowingCategory(thisMonth, lastMonth, baseCurrency));

        // 3. Dita më e shtrenjtë e javës
        result.put("mostExpensiveDay", findMostExpensiveDay(thisMonth, baseCurrency));

        // 4. Norma e kursimit
        if (thisMonthIncome > 0) {
            double savingsRate = ((thisMonthIncome - thisMonthExpense) / thisMonthIncome) * 100;
            result.put("savingsRate", Math.round(savingsRate));
        } else {
            result.put("savingsRate", null);
        }

        // 5. Mesatarja ditore
        result.put("dailyAverage", Math.round(thisMonthExpense / dayOfMonth));

        return result;
    }

    private double sumExpenses(List<Transaction> transactions, String baseCurrency) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

    private double sumIncome(List<Transaction> transactions, String baseCurrency) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

    private String findMostExpensiveDay(List<Transaction> transactions, String baseCurrency) {
        Map<DayOfWeek, Double> byDay = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                double amount = exchangeRateService.convert(
                        t.getAmount(), t.getCurrency().name(), baseCurrency).doubleValue();
                byDay.merge(t.getDate().getDayOfWeek(), amount, Double::sum);
            }
        }
        return byDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().toString())
                .orElse(null);
    }

    private String findTopGrowingCategory(List<Transaction> thisMonth, List<Transaction> lastMonth, String baseCurrency) {
        Map<String, Double> thisMonthByCat = sumByCategory(thisMonth, baseCurrency);
        Map<String, Double> lastMonthByCat = sumByCategory(lastMonth, baseCurrency);

        String topCategory = null;
        double maxGrowth = 0;

        for (Map.Entry<String, Double> entry : thisMonthByCat.entrySet()) {
            double thisAmount = entry.getValue();
            double lastAmount = lastMonthByCat.getOrDefault(entry.getKey(), 0.0);
            double growth = thisAmount - lastAmount;
            if (growth > maxGrowth) {
                maxGrowth = growth;
                topCategory = entry.getKey();
            }
        }
        return topCategory;
    }

    private Map<String, Double> sumByCategory(List<Transaction> transactions, String baseCurrency) {
        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCategory() != null) {
                double amount = exchangeRateService.convert(
                        t.getAmount(), t.getCurrency().name(), baseCurrency).doubleValue();
                byCategory.merge(t.getCategory().getName(), amount, Double::sum);
            }
        }
        return byCategory;
    }
}