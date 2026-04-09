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

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCurrency(request.getCurrency());

        userRepository.save(user);
        createDefaultCategories(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
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