package com.moneto.api.controller;

import com.moneto.api.dto.AuthResponse;
import com.moneto.api.dto.LoginRequest;
import com.moneto.api.dto.RegisterRequest;
import com.moneto.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String code) {
        authService.verify(email, code);
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }
}