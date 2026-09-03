package com.moneto.api.service;

import com.moneto.api.model.*;
import com.moneto.api.repository.CategoryRepository;
import com.moneto.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final AuditService auditService;

    public Map<String, Object> importTransactions(MultipartFile file) {
        User user = userService.getCurrentUser();

        int imported = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Anashkalo rreshtin e parë (header) dhe rreshtat bosh
                if (lineNumber == 1 || line.trim().isEmpty()) continue;

                try {
                    String[] parts = line.split(",");
                    if (parts.length < 4) {
                        errors.add("Line " + lineNumber + ": not enough columns");
                        continue;
                    }

                    LocalDate date = LocalDate.parse(parts[0].trim());
                    if (date.isAfter(LocalDate.now())) {
                        errors.add("Line " + lineNumber + ": date is in the future");
                        continue;
                    }
                    TransactionType type = TransactionType.valueOf(parts[1].trim().toUpperCase());
                    String categoryName = parts[2].trim();
                    BigDecimal amount = new BigDecimal(parts[3].trim());

                    Currency currency = Currency.USD;
                    if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                        currency = Currency.valueOf(parts[4].trim().toUpperCase());
                    }

                    String description = parts.length >= 6 ? parts[5].trim() : "";

                    // Gjej ose krijo kategorinë
                    Category category = findOrCreateCategory(user, categoryName, type);

                    Transaction transaction = new Transaction();
                    transaction.setUser(user);
                    transaction.setCategory(category);
                    transaction.setDate(date);
                    transaction.setType(type);
                    transaction.setAmount(amount);
                    transaction.setCurrency(currency);
                    transaction.setDescription(description);
                    transaction.setIsRecurring(false);

                    transactionRepository.save(transaction);
                    imported++;

                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }

        auditService.logCurrentUser("IMPORT_TRANSACTIONS",
                "Imported " + imported + " transactions, " + errors.size() + " errors");

        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("errors", errors);
        return result;
    }

    private Category findOrCreateCategory(User user, String name, TransactionType type) {
        List<Category> categories = categoryRepository.findByUserId(user.getId());
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        // Krijo kategori të re nëse s'ekziston
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setColor("#888888");
        category.setIcon("default");
        category.setType(type);
        return categoryRepository.save(category);
    }
}