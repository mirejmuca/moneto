package com.moneto.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private com.moneto.api.repository.TransactionRepository transactionRepository;
    @Mock
    private ExchangeRateService exchangeRateService;
    @Mock
    private UserService userService;
    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private ForecastService forecastService;

    @Test
    void regression_perfectlyLinear_returnsExactSlope() {
        // Vlera 100, 200, 300 → rritje konstante 100 → slope duhet 100
        List<Double> values = List.of(100.0, 200.0, 300.0);

        double[] result = forecastService.calculateRegression(values);

        assertEquals(100.0, result[0], 0.0001); // slope
        assertEquals(100.0, result[1], 0.0001); // intercept (vlera te x=0)
    }

    @Test
    void regression_flatValues_returnsZeroSlope() {
        // Vlera të njëjta → s'ka tendencë → slope duhet 0
        List<Double> values = List.of(500.0, 500.0, 500.0);

        double[] result = forecastService.calculateRegression(values);

        assertEquals(0.0, result[0], 0.0001); // slope
        assertEquals(500.0, result[1], 0.0001); // intercept
    }

    @Test
    void regression_decreasing_returnsNegativeSlope() {
        // Vlera zbritëse 300, 200, 100 → slope duhet -100
        List<Double> values = List.of(300.0, 200.0, 100.0);

        double[] result = forecastService.calculateRegression(values);

        assertEquals(-100.0, result[0], 0.0001);
    }

    @Test
    void regression_singleValue_returnsZeroSlopeAndThatValue() {
        // Vetëm një vlerë → s'mund të llogaritet tendencë → slope 0, intercept = vlera
        List<Double> values = List.of(250.0);

        double[] result = forecastService.calculateRegression(values);

        assertEquals(0.0, result[0], 0.0001);
        assertEquals(250.0, result[1], 0.0001);
    }

    @Test
    void regression_empty_returnsZeros() {
        List<Double> values = List.of();

        double[] result = forecastService.calculateRegression(values);

        assertEquals(0.0, result[0], 0.0001);
        assertEquals(0.0, result[1], 0.0001);
    }
}