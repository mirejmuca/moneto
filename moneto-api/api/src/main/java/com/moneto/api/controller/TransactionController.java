package com.moneto.api.controller;

import com.moneto.api.model.Transaction;
import com.moneto.api.model.TransactionType;
import com.moneto.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> getByType(@RequestParam TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(type));
    }

    @GetMapping("/range")
    public ResponseEntity<List<Transaction>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(start, end));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType type,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Boolean isRecurring) {
        return ResponseEntity.ok(transactionService.createTransaction(
                categoryId, amount, type, description, date, isRecurring));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, amount, description, date));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction deleted");
    }
}
