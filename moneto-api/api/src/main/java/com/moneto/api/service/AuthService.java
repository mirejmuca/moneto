package com.moneto.api.service;

import com.moneto.api.config.JwtService;
import com.moneto.api.dto.AuthResponse;
import com.moneto.api.dto.LoginRequest;
import com.moneto.api.dto.RegisterRequest;
import com.moneto.api.model.Category;
import com.moneto.api.model.TransactionType;
import com.moneto.api.model.User;
import com.moneto.api.repository.CategoryRepository;
import com.moneto.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditService auditService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCurrency(request.getCurrency());
        user.setIsVerified(false);
        user.setVerificationToken(generateVerificationCode());

        userRepository.save(user);
        createDefaultCategories(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        auditService.log(user.getEmail(), "REGISTER", null);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(user.getEmail());

        auditService.log(user.getEmail(), "LOGIN", null);

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getCurrency(), user.getRole());
    }

    public void verify(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Account already verified");
        }

        if (!code.equals(user.getVerificationToken())) {
            throw new RuntimeException("Invalid verification code");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    private String generateVerificationCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    private void createDefaultCategories(User user) {
        List<String[]> defaults = List.of(
                new String[]{"Food", "#FF6B6B", "food", "EXPENSE"},
                new String[]{"Transport", "#4ECDC4", "transport", "EXPENSE"},
                new String[]{"Rent", "#45B7D1", "rent", "EXPENSE"},
                new String[]{"Entertainment", "#96CEB4", "entertainment", "EXPENSE"},
                new String[]{"Health", "#FFEAA7", "health", "EXPENSE"},
                new String[]{"Shopping", "#DDA0DD", "shopping", "EXPENSE"},
                new String[]{"Salary", "#98FB98", "salary", "INCOME"},
                new String[]{"Freelance", "#87CEEB", "freelance", "INCOME"}
        );

        for (String[] d : defaults) {
            Category category = new Category();
            category.setUser(user);
            category.setName(d[0]);
            category.setColor(d[1]);
            category.setIcon(d[2]);
            category.setType(TransactionType.valueOf(d[3]));
            categoryRepository.save(category);
        }
    }
}