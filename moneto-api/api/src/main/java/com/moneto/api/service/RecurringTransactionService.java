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
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public List<RecurringTransaction> getAllRecurring() {
        User user = userService.getCurrentUser();
        return recurringTransactionRepository.findByUserId(user.getId());
    }

    public RecurringTransaction createRecurring(Long categoryId, BigDecimal amount,
                                                TransactionType type, String description,
                                                Frequency frequency, LocalDate startDate) {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setAmount(amount);
        recurring.setType(type);
        recurring.setDescription(description);
        recurring.setFrequency(frequency);
        recurring.setNextDueDate(startDate);
        recurring.setIsActive(true);

        return recurringTransactionRepository.save(recurring);
    }

    public RecurringTransaction updateRecurring(Long id, BigDecimal amount,
                                                String description, Frequency frequency) {
        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        if (amount != null) recurring.setAmount(amount);
        if (description != null) recurring.setDescription(description);
        if (frequency != null) recurring.setFrequency(frequency);
        return recurringTransactionRepository.save(recurring);
    }

    public void cancelRecurring(Long id) {
        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        recurring.setIsActive(false);
        recurringTransactionRepository.save(recurring);
    }

    public void processRecurring() {
        List<RecurringTransaction> due = recurringTransactionRepository
                .findByIsActiveTrueAndNextDueDateBefore(LocalDate.now().plusDays(1));

        for (RecurringTransaction recurring : due) {
            Transaction transaction = new Transaction();
            transaction.setUser(recurring.getUser());
            transaction.setCategory(recurring.getCategory());
            transaction.setAmount(recurring.getAmount());
            transaction.setType(recurring.getType());
            transaction.setDescription(recurring.getDescription());
            transaction.setDate(recurring.getNextDueDate());
            transaction.setIsRecurring(true);
            transactionRepository.save(transaction);

            recurring.setNextDueDate(calculateNextDate(recurring));
            recurringTransactionRepository.save(recurring);

            if (recurring.getUser().getRecurringReminders()) {
                Notification notification = new Notification();
                notification.setUser(recurring.getUser());
                notification.setMessage("Recurring transaction processed: " +
                        recurring.getDescription() + " - " + recurring.getAmount());
                notificationRepository.save(notification);
            }
        }
    }

    private LocalDate calculateNextDate(RecurringTransaction recurring) {
        return switch (recurring.getFrequency()) {
            case WEEKLY -> recurring.getNextDueDate().plusWeeks(1);
            case MONTHLY -> recurring.getNextDueDate().plusMonths(1);
            case YEARLY -> recurring.getNextDueDate().plusYears(1);
        };
    }
}