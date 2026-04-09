package com.moneto.api.service;

import com.moneto.api.model.Category;
import com.moneto.api.model.TransactionType;
import com.moneto.api.model.User;
import com.moneto.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public List<Category> getAllCategories() {
        User user = userService.getCurrentUser();
        return categoryRepository.findByUserId(user.getId());
    }

    public Category createCategory(String name, String color, String icon, TransactionType type) {
        User user = userService.getCurrentUser();
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setColor(color);
        category.setIcon(icon);
        category.setType(type);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, String name, String color, String icon) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (name != null) category.setName(name);
        if (color != null) category.setColor(color);
        if (icon != null) category.setIcon(icon);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}