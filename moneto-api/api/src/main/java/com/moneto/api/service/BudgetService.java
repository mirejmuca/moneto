package com.moneto.api.service;

import com.moneto.api.model.Budget;
import com.moneto.api.model.Category;
import com.moneto.api.model.Transaction;
import com.moneto.api.model.User;
import com.moneto.api.repository.BudgetRepository;
import com.moneto.api.repository.CategoryRepository;
import com.moneto.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<Budget> getBudgets(Integer month, Integer year) {
        User user = userService.getCurrentUser();
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();
        return budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year);
    }

    public Budget createBudget(Long categoryId, BigDecimal amount, Integer month, Integer year) {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(amount);
        budget.setMonth(month);
        budget.setYear(year);
        return budgetRepository.save(budget);
    }

    public Budget updateBudget(Long id, BigDecimal amount) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budget.setAmount(amount);
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    public Map<String, Object> getBudgetStatus(Integer month, Integer year) {
        User user = userService.getCurrentUser();
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(
                user.getId(), month, year);

        Map<String, Object> status = new HashMap<>();

        for (Budget budget : budgets) {
            // Vetëm transaksionet e muajit dhe vitit të buxhetit
            LocalDate monthStart = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndCategoryIdAndDateBetween(
                            user.getId(), budget.getCategory().getId(), monthStart, monthEnd);

            BigDecimal spent = transactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = spent.divide(budget.getAmount(), 4,
                            java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            Map<String, Object> categoryStatus = new HashMap<>();
            categoryStatus.put("budgetId", budget.getId());
            categoryStatus.put("category", budget.getCategory().getName());
            categoryStatus.put("budgetAmount", budget.getAmount());
            categoryStatus.put("spent", spent);
            categoryStatus.put("percentage", Math.min(percentage, 100));
            categoryStatus.put("exceeded", spent.compareTo(budget.getAmount()) > 0);

            status.put(budget.getCategory().getName(), categoryStatus);
        }

        return status;
    }
}