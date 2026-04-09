package com.moneto.api.scheduler;

import com.moneto.api.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class TransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringTransactions() {
        recurringTransactionService.processRecurring();
    }
}