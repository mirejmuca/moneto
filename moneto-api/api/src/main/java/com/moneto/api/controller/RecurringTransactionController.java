package com.moneto.api.controller;

import com.moneto.api.model.Frequency;
import com.moneto.api.model.RecurringTransaction;
import com.moneto.api.model.TransactionType;
import com.moneto.api.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getAllRecurring() {
        return ResponseEntity.ok(recurringTransactionService.getAllRecurring());
    }

    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurring(
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType type,
            @RequestParam(required = false) String description,
            @RequestParam Frequency frequency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(recurringTransactionService.createRecurring(
                categoryId, amount, type, description, frequency, startDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurring(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Frequency frequency) {
        return ResponseEntity.ok(recurringTransactionService.updateRecurring(
                id, amount, description, frequency));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelRecurring(@PathVariable Long id) {
        recurringTransactionService.cancelRecurring(id);
        return ResponseEntity.ok("Recurring transaction cancelled");
    }
}