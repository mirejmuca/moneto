package com.moneto.api.controller;

import com.moneto.api.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> importTransactions(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importTransactions(file));
    }
}