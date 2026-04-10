package com.moneto.api.scheduler;

import com.moneto.api.repository.UserRepository;
import com.moneto.api.service.RecurringTransactionService;
import com.moneto.api.service.SavingGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class TransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;
    private final UserRepository userRepository;
    private final SavingGoalService savingGoalService;

    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringTransactions() {
        recurringTransactionService.processRecurring();
    }

    @Scheduled(cron = "0 0 1 1 * *")
    public void processEndOfMonth() {
        userRepository.findAll().forEach(user ->
                savingGoalService.processEndOfMonth(user.getId()));
    }
}