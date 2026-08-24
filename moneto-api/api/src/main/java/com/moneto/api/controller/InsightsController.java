package com.moneto.api.controller;

import com.moneto.api.service.InsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getInsights() {
        return ResponseEntity.ok(insightsService.getInsights());
    }
}