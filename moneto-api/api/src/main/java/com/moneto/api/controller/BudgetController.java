package com.moneto.api.controller;

import com.moneto.api.model.Budget;
import com.moneto.api.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<Budget>> getBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(budgetService.getBudgets(month, year));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBudgetStatus(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(budgetService.getBudgetStatus(month, year));
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @RequestParam Long categoryId,
            @RequestParam BigDecimal amount,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(budgetService.createBudget(categoryId, amount, month, year));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id,
                                               @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(budgetService.updateBudget(id, amount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok("Budget deleted");
    }
}