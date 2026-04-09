package com.moneto.api.service;

import com.moneto.api.model.*;
import com.moneto.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public List<Transaction> getAllTransactions() {
        User user = userService.getCurrentUser();
        return transactionRepository.findByUserId(user.getId());
    }

    public List<Transaction> getTransactionsByType(TransactionType type) {
        User user = userService.getCurrentUser();
        return transactionRepository.findByUserIdAndType(user.getId(), type);
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        User user = userService.getCurrentUser();
        return transactionRepository.findByUserIdAndDateBetween(user.getId(), start, end);
    }

    public Transaction createTransaction(Long categoryId, BigDecimal amount,
                                         TransactionType type, String description,
                                         LocalDate date, Boolean isRecurring) {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setDate(date);
        transaction.setIsRecurring(isRecurring != null && isRecurring);

        Transaction saved = transactionRepository.save(transaction);

        if (type == TransactionType.EXPENSE) {
            checkBudget(user, category, amount);
        }

        return saved;
    }

    public Transaction updateTransaction(Long id, BigDecimal amount,
                                         String description, LocalDate date) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (amount != null) transaction.setAmount(amount);
        if (description != null) transaction.setDescription(description);
        if (date != null) transaction.setDate(date);
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    private void checkBudget(User user, Category category, BigDecimal amount) {
        if (!user.getBudgetAlerts()) return;

        LocalDate now = LocalDate.now();
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), category.getId(), now.getMonthValue(), now.getYear()
        ).ifPresent(budget -> {
            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndCategoryId(user.getId(), category.getId());

            BigDecimal total = transactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (total.compareTo(budget.getAmount()) > 0) {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setMessage("You have exceeded your " +
                        category.getName() + " budget for this month.");
                notificationRepository.save(notification);
            }
        });
    }
}