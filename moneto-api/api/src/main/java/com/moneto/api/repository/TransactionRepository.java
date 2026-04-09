package com.moneto.api.repository;

import com.moneto.api.model.Transaction;
import com.moneto.api.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);
}