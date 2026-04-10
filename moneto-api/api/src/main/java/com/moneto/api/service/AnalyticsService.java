package com.moneto.api.service;

import com.moneto.api.model.*;
import com.moneto.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final UserService userService;

    public Map<String, Object> getMonthlySummary() {
        User user = userService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), start, end);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        List<SavingGoal> activeGoals = savingGoalRepository
                .findByUserIdAndStatus(user.getId(), SavingGoalStatus.IN_PROGRESS);

        BigDecimal totalSaved = activeGoals.stream()
                .map(SavingGoal::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netBalance", netBalance);
        summary.put("totalSaved", totalSaved);
        summary.put("activeGoals", activeGoals.size());

        return summary;
    }

    public Map<String, BigDecimal> getSpendingByCategory() {
        User user = userService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), start, end);

        Map<String, BigDecimal> byCategory = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                String category = t.getCategory().getName();
                byCategory.merge(category, t.getAmount(), BigDecimal::add);
            }
        }

        return byCategory;
    }

    public List<Map<String, Object>> getMonthlyTrend(int months) {
        User user = userService.getCurrentUser();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            LocalDate start = date.withDayOfMonth(1);
            LocalDate end = date.withDayOfMonth(date.lengthOfMonth());

            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateBetween(user.getId(), start, end);

            BigDecimal income = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenses = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> month = new HashMap<>();
            month.put("month", date.getMonth().toString());
            month.put("year", date.getYear());
            month.put("income", income);
            month.put("expenses", expenses);
            trend.add(month);
        }

        return trend;
    }

    public Map<String, Object> getTopCategory() {
        Map<String, BigDecimal> byCategory = getSpendingByCategory();
        if (byCategory.isEmpty()) return Collections.emptyMap();

        String topCategory = Collections.max(byCategory.entrySet(),
                Map.Entry.comparingByValue()).getKey();

        Map<String, Object> result = new HashMap<>();
        result.put("category", topCategory);
        result.put("amount", byCategory.get(topCategory));
        return result;
    }
}