package com.moneto.api.controller;

import com.moneto.api.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMonthlySummary() {
        return ResponseEntity.ok(analyticsService.getMonthlySummary());
    }

    @GetMapping("/by-category")
    public ResponseEntity<Map<String, BigDecimal>> getSpendingByCategory() {
        return ResponseEntity.ok(analyticsService.getSpendingByCategory());
    }

    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyTrend(months));
    }

    @GetMapping("/top-category")
    public ResponseEntity<Map<String, Object>> getTopCategory() {
        return ResponseEntity.ok(analyticsService.getTopCategory());
    }
}