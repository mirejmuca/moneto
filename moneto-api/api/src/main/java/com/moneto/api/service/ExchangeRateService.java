package com.moneto.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    @Value("${exchange.rate.api.key}")
    private String apiKey;

    @Value("${exchange.rate.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // Cache i kurseve të fundit të suksesshme, si fallback nëse API-ja bie
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return amount;

        String cacheKey = fromCurrency + "_" + toCurrency;
        double rate;

        try {
            String url = apiUrl + apiKey + "/pair/" + fromCurrency + "/" + toCurrency;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.get("result").equals("success")) {
                throw new RuntimeException("API returned unsuccessful result");
            }

            rate = ((Number) response.get("conversion_rate")).doubleValue();
            // Ruaj kursin e suksesshëm te cache për përdorim të mëvonshëm si fallback
            rateCache.put(cacheKey, rate);

        } catch (Exception e) {
            // API-ja dështoi — provo të përdorësh kursin e fundit të njohur
            Double cachedRate = rateCache.get(cacheKey);
            if (cachedRate != null) {
                rate = cachedRate;
            } else {
                // S'ka kurs të ruajtur për këtë çift — s'mund të konvertojmë me saktësi
                throw new RuntimeException("Exchange rate unavailable and no cached rate for " + cacheKey);
            }
        }

        return amount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}