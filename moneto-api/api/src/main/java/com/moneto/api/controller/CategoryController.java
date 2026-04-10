package com.moneto.api.controller;

import com.moneto.api.model.Category;
import com.moneto.api.model.TransactionType;
import com.moneto.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestParam String name,
                                                   @RequestParam String color,
                                                   @RequestParam String icon,
                                                   @RequestParam TransactionType type) {
        return ResponseEntity.ok(categoryService.createCategory(name, color, icon, type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String color,
                                                   @RequestParam(required = false) String icon) {
        return ResponseEntity.ok(categoryService.updateCategory(id, name, color, icon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted");
    }
}