package com.moneto.api.controller;

import com.moneto.api.model.Alert;
import com.moneto.api.model.AlertType;
import com.moneto.api.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(alertService.getAlerts());
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(
            @RequestParam AlertType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam BigDecimal threshold) {
        return ResponseEntity.ok(alertService.createAlert(type, categoryId, threshold));
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long alertId) {
        alertService.deleteAlert(alertId);
        return ResponseEntity.ok().build();
    }
}