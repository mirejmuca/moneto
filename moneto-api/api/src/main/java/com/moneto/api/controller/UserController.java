package com.moneto.api.controller;

import com.moneto.api.model.User;
import com.moneto.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestParam String name,
                                              @RequestParam String email) {
        return ResponseEntity.ok(userService.updateProfile(name, email));
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestParam String oldPassword,
                                                 @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/settings")
    public ResponseEntity<User> updateSettings(@RequestParam(required = false) String currency,
                                               @RequestParam(required = false) Integer monthStart,
                                               @RequestParam(required = false) Boolean budgetAlerts,
                                               @RequestParam(required = false) Boolean recurringReminders) {
        return ResponseEntity.ok(userService.updateSettings(currency, monthStart, budgetAlerts, recurringReminders));
    }

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok("Account deleted successfully");
    }
}
