package com.moneto.api.controller;

import com.moneto.api.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getForecast() {
        return ResponseEntity.ok(forecastService.getForecast());
    }
}