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
public class SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public List<SavingGoal> getAllGoals() {
        User user = userService.getCurrentUser();
        return savingGoalRepository.findByUserId(user.getId());
    }

    public List<SavingGoal> getActiveGoals() {
        User user = userService.getCurrentUser();
        return savingGoalRepository.findByUserIdAndStatus(user.getId(), SavingGoalStatus.IN_PROGRESS);
    }

    public SavingGoal createGoal(String name, BigDecimal targetAmount, LocalDate deadline) {
        User user = userService.getCurrentUser();
        SavingGoal goal = new SavingGoal();
        goal.setUser(user);
        goal.setName(name);
        goal.setTargetAmount(targetAmount);
        goal.setDeadline(deadline);
        return savingGoalRepository.save(goal);
    }

    public SavingGoal updateGoal(Long id, String name, BigDecimal targetAmount, LocalDate deadline) {
        SavingGoal goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));
        if (name != null) goal.setName(name);
        if (targetAmount != null) goal.setTargetAmount(targetAmount);
        if (deadline != null) goal.setDeadline(deadline);
        return savingGoalRepository.save(goal);
    }

    public void cancelGoal(Long id) {
        SavingGoal goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));
        goal.setStatus(SavingGoalStatus.CANCELLED);
        savingGoalRepository.save(goal);
    }

    public void addToGoal(Long id, BigDecimal amount) {
        SavingGoal goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingGoalStatus.COMPLETED);
            Notification notification = new Notification();
            notification.setUser(goal.getUser());
            notification.setMessage("Congratulations! You reached your savings goal: " + goal.getName());
            notificationRepository.save(notification);
        }

        savingGoalRepository.save(goal);
    }

    public void processEndOfMonth(Long userId) {
        User user = userService.getCurrentUser();
        List<SavingGoal> activeGoals = savingGoalRepository
                .findByUserIdAndStatus(userId, SavingGoalStatus.IN_PROGRESS);

        if (activeGoals.isEmpty()) return;

        LocalDate now = LocalDate.now();
        List<Budget> budgets = budgetRepository
                .findByUserIdAndMonthAndYear(userId, now.getMonthValue(), now.getYear());

        BigDecimal totalLeftover = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndCategoryId(userId, budget.getCategory().getId());

            BigDecimal spent = transactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal leftover = budget.getAmount().subtract(spent);
            if (leftover.compareTo(BigDecimal.ZERO) > 0) {
                totalLeftover = totalLeftover.add(leftover);
            }
        }

        if (totalLeftover.compareTo(BigDecimal.ZERO) > 0) {
            SavingGoal goal = activeGoals.get(0);
            addToGoal(goal.getId(), totalLeftover);

            if (user.getRecurringReminders()) {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setMessage("End of month: " + totalLeftover +
                        " leftover budget added to your goal: " + goal.getName());
                notificationRepository.save(notification);
            }
        }
    }
}