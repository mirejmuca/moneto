package com.moneto.api.controller;

import com.moneto.api.model.Role;
import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> changeRole(@PathVariable Long userId, @RequestParam Role role) {
        adminService.changeRole(userId, role);
        return ResponseEntity.ok("Role updated successfully");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/users/{userId}/subscription")
    public ResponseEntity<String> changeSubscription(@PathVariable Long userId,
                                                     @RequestParam SubscriptionTier tier) {
        adminService.changeSubscription(userId, tier);
        return ResponseEntity.ok("Subscription updated successfully");
    }
}