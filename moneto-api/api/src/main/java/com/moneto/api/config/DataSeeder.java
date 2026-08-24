package com.moneto.api.config;

import com.moneto.api.model.Currency;
import com.moneto.api.model.Role;
import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.model.User;
import com.moneto.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // Kontrollo nëse ekziston tashmë një admin
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == Role.ADMIN);

        if (!adminExists) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setCurrency(Currency.USD);
            admin.setRole(Role.ADMIN);
            admin.setIsVerified(true); // admin-i s'ka nevojë të verifikojë email
            admin.setSubscriptionTier(SubscriptionTier.FREE);

            userRepository.save(admin);
            System.out.println("✓ Admin account created: " + adminEmail);
        }
    }
}