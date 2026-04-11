package com.moneto.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    @Value("${exchange.rate.api.key}")
    private String apiKey;

    @Value("${exchange.rate.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return amount;

        String url = apiUrl + apiKey + "/pair/" + fromCurrency + "/" + toCurrency;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.get("result").equals("success")) {
            throw new RuntimeException("Failed to fetch exchange rate");
        }

        double rate = ((Number) response.get("conversion_rate")).doubleValue();
        return amount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}