package com.moneto.api.service;

import com.moneto.api.model.Transaction;
import com.moneto.api.model.TransactionType;
import com.moneto.api.model.User;
import com.moneto.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final com.moneto.api.model.SubscriptionTier PLUS = com.moneto.api.model.SubscriptionTier.PLUS;

    public Map<String, Object> getForecast() {
        subscriptionService.requireTier(PLUS);

        User user = userService.getCurrentUser();

        int monthsBack = 6;
        List<Double> monthlyTotals = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();

        for (int i = monthsBack - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            LocalDate start = date.withDayOfMonth(1);
            LocalDate end = date.withDayOfMonth(date.lengthOfMonth());

            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateBetween(user.getId(), start, end);

            double total = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(t -> exchangeRateService.convert(
                            t.getAmount(), t.getCurrency().name(), user.getCurrency().name()))
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();

            monthlyTotals.add(total);
            monthLabels.add(date.getMonth().toString());
        }

        // Llogarit slope dhe intercept një herë
        double[] regression = calculateRegression(monthlyTotals);
        double slope = regression[0];
        double intercept = regression[1];

        int n = monthlyTotals.size();

        // Gjenero 3 parashikime
        List<Double> forecasts = new ArrayList<>();
        List<String> forecastLabels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double predicted = slope * (n + i) + intercept;
            forecasts.add(Math.max(0, predicted));
            forecastLabels.add(LocalDate.now().plusMonths(i + 1).getMonth().toString());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("history", monthlyTotals);
        result.put("labels", monthLabels);
        result.put("forecasts", forecasts);
        result.put("forecastLabels", forecastLabels);
        return result;
    }

    double[] calculateRegression(List<Double> values) {
        int n = values.size();
        if (n < 2) {
            double val = n == 1 ? values.get(0) : 0;
            return new double[]{0, val};
        }

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        return new double[]{slope, intercept};
    }

}