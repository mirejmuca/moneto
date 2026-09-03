package com.moneto.api.service;

import com.moneto.api.model.*;
import com.moneto.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final ExchangeRateService exchangeRateService;
    private final ForecastService forecastService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";

    public String ask(String question) {
        subscriptionService.requireTier(SubscriptionTier.PREMIUM);
        User user = userService.getCurrentUser();

        String context = buildFinancialContext(user);
        String prompt = buildPrompt(context, question);

        return callGemini(prompt);
    }

    private String buildFinancialContext(User user) {
        String baseCurrency = user.getCurrency().name();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), monthStart, now);

        double income = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue).sum();

        double expense = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue).sum();

        // Shpenzimet sipas kategorive
        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction t : monthTransactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCategory() != null) {
                double amt = exchangeRateService.convert(
                        t.getAmount(), t.getCurrency().name(), baseCurrency).doubleValue();
                byCategory.merge(t.getCategory().getName(), amt, Double::sum);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("User's financial summary for this month (currency: ").append(baseCurrency).append("):\n");
        sb.append("- Total income: ").append(String.format("%.2f", income)).append("\n");
        sb.append("- Total expenses: ").append(String.format("%.2f", expense)).append("\n");
        sb.append("- Balance: ").append(String.format("%.2f", income - expense)).append("\n");
        sb.append("- Spending by category:\n");
        byCategory.forEach((cat, amt) ->
                sb.append("  * ").append(cat).append(": ").append(String.format("%.2f", amt)).append("\n"));

        // Buxhetet
        List<Budget> budgets = budgetRepository.findByUserId(user.getId());
        if (!budgets.isEmpty()) {
            sb.append("- Budgets:\n");
            budgets.forEach(b -> sb.append("  * ").append(b.getCategory().getName())
                    .append(": ").append(b.getAmount()).append("\n"));
        }

        // Qëllimet e kursimit
        List<SavingGoal> goals = savingGoalRepository
                .findByUserIdAndStatus(user.getId(), SavingGoalStatus.IN_PROGRESS);
        if (!goals.isEmpty()) {
            sb.append("- Saving goals:\n");
            goals.forEach(g -> sb.append("  * ").append(g.getName())
                    .append(": ").append(g.getCurrentAmount()).append("/").append(g.getTargetAmount()).append("\n"));
        }

        // Parashikimi i shpenzimeve për 3 muajt e ardhshëm
        try {
            Map<String, Object> forecast = forecastService.getForecast();
            List<Double> forecasts = (List<Double>) forecast.get("forecasts");
            List<String> forecastLabels = (List<String>) forecast.get("forecastLabels");
            if (forecasts != null && !forecasts.isEmpty()) {
                sb.append("- Predicted expenses for upcoming months:\n");
                for (int i = 0; i < forecasts.size(); i++) {
                    sb.append("  * ").append(forecastLabels.get(i))
                            .append(": ").append(String.format("%.2f", forecasts.get(i))).append("\n");
                }
            }
        } catch (Exception e) {
            // nëse s'ka mjaftueshëm të dhëna për parashikim, thjesht e anashkalojmë
        }

        // Krahasimi me muajin e kaluar
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);

        List<Transaction> lastMonthTransactions = transactionRepository
                .findByUserIdAndDateBetween(user.getId(), lastMonthStart, lastMonthEnd);

        double lastMonthExpense = lastMonthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> exchangeRateService.convert(t.getAmount(), t.getCurrency().name(), baseCurrency))
                .mapToDouble(BigDecimal::doubleValue).sum();

        sb.append("- Last month's total expenses: ").append(String.format("%.2f", lastMonthExpense)).append("\n");

        if (lastMonthExpense > 0) {
            double changePercent = ((expense - lastMonthExpense) / lastMonthExpense) * 100;
            sb.append("- Change vs last month: ")
                    .append(String.format("%+.1f%%", changePercent))
                    .append(expense > lastMonthExpense ? " (spending more)" : " (spending less)")
                    .append("\n");
        }

        // Shpenzimet e muajit të kaluar sipas kategorive (për krahasim dhe sugjerime)
        Map<String, Double> lastMonthByCategory = new HashMap<>();
        for (Transaction t : lastMonthTransactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCategory() != null) {
                double amt = exchangeRateService.convert(
                        t.getAmount(), t.getCurrency().name(), baseCurrency).doubleValue();
                lastMonthByCategory.merge(t.getCategory().getName(), amt, Double::sum);
            }
        }
        if (!lastMonthByCategory.isEmpty()) {
            sb.append("- Last month's spending by category:\n");
            lastMonthByCategory.forEach((cat, amt) ->
                    sb.append("  * ").append(cat).append(": ").append(String.format("%.2f", amt)).append("\n"));
        }

        return sb.toString();

    }

    private String buildPrompt(String context, String question) {
        return "You are a helpful financial assistant for the Moneto app. " +
                "Answer the user's question based only on their financial data below. " +
                "Be concise, friendly, and give practical advice when relevant. " +
                "When useful, compare this month's spending to last month's and point out categories " +
                "where the user could save money or is overspending. " +
                "If the question is unrelated to personal finance, politely redirect. " +
                "Always reply in the same language as the user's question.\n\n" +
                context + "\nUser's question: " + question;
    }

    private String callGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String url = GEMINI_URL + "?key=" + geminiApiKey;
            Map response = restTemplate.postForObject(url, request, Map.class);

            List<Map> candidates = (List<Map>) response.get("candidates");
            Map firstCandidate = candidates.get(0);
            Map contentResp = (Map) firstCandidate.get("content");
            List<Map> parts = (List<Map>) contentResp.get("parts");


            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}