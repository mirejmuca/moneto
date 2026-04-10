package com.moneto.api.controller;

import com.moneto.api.model.SavingGoal;
import com.moneto.api.service.SavingGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @GetMapping
    public ResponseEntity<List<SavingGoal>> getAllGoals() {
        return ResponseEntity.ok(savingGoalService.getAllGoals());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SavingGoal>> getActiveGoals() {
        return ResponseEntity.ok(savingGoalService.getActiveGoals());
    }

    @PostMapping
    public ResponseEntity<SavingGoal> createGoal(
            @RequestParam String name,
            @RequestParam BigDecimal targetAmount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {
        return ResponseEntity.ok(savingGoalService.createGoal(name, targetAmount, deadline));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingGoal> updateGoal(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal targetAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {
        return ResponseEntity.ok(savingGoalService.updateGoal(id, name, targetAmount, deadline));
    }

    @PutMapping("/{id}/add")
    public ResponseEntity<String> addToGoal(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        savingGoalService.addToGoal(id, amount);
        return ResponseEntity.ok("Amount added to goal");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelGoal(@PathVariable Long id) {
        savingGoalService.cancelGoal(id);
        return ResponseEntity.ok("Goal cancelled");
    }
}