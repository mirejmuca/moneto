package com.moneto.api.controller;

import com.moneto.api.dto.UserDto;
import com.moneto.api.model.User;
import com.moneto.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile() {
        return ResponseEntity.ok(toDto(userService.getCurrentUser()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@RequestParam String name,
                                                 @RequestParam String email) {
        return ResponseEntity.ok(toDto(userService.updateProfile(name, email)));
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestParam String oldPassword,
                                                 @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/settings")
    public ResponseEntity<UserDto> updateSettings(@RequestParam(required = false) String currency,
                                                  @RequestParam(required = false) Integer monthStart,
                                                  @RequestParam(required = false) Boolean budgetAlerts,
                                                  @RequestParam(required = false) Boolean recurringReminders) {
        return ResponseEntity.ok(toDto(userService.updateSettings(currency, monthStart, budgetAlerts, recurringReminders)));
    }

    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok("Account deleted successfully");
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCurrency(),
                user.getMonthStart(),
                user.getBudgetAlerts(),
                user.getRecurringReminders(),
                user.getIsVerified(),
                user.getRole(),
                user.getSubscriptionTier(),
                user.getSubscriptionExpiresAt()
        );
    }
}