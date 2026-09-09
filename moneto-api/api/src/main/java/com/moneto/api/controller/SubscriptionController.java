package com.moneto.api.controller;

import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(subscriptionService.getSubscriptionStatus());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(
            @RequestParam SubscriptionTier tier,
            @RequestParam(required = false) String cardLast4) {
        return ResponseEntity.ok(subscriptionService.subscribe(tier, cardLast4));
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancel() {
        subscriptionService.cancelSubscription();
        return ResponseEntity.ok("Subscription cancelled");
    }

    @GetMapping("/plans")
    public ResponseEntity<List<Map<String, Object>>> getPlans() {
        return ResponseEntity.ok(subscriptionService.getPlans());
    }
}