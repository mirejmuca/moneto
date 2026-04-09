package com.moneto.api.repository;

import com.moneto.api.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserId(Long userId);
    List<RecurringTransaction> findByIsActiveTrueAndNextDueDateBefore(LocalDate date);
}