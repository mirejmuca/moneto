package com.moneto.api.service;

import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.model.User;
import com.moneto.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    // ---- getEffectiveTier ----

    @Test
    void effectiveTier_isFree_whenUserIsFree() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.FREE);

        assertEquals(SubscriptionTier.FREE, subscriptionService.getEffectiveTier(user));
    }

    @Test
    void effectiveTier_isFree_whenSubscriptionExpired() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setSubscriptionExpiresAt(LocalDate.now().minusDays(1)); // skadoi dje

        assertEquals(SubscriptionTier.FREE, subscriptionService.getEffectiveTier(user));
    }

    @Test
    void effectiveTier_isFree_whenExpiryIsNull() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setSubscriptionExpiresAt(null);

        assertEquals(SubscriptionTier.FREE, subscriptionService.getEffectiveTier(user));
    }

    @Test
    void effectiveTier_isPremium_whenActiveAndNotExpired() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setSubscriptionExpiresAt(LocalDate.now().plusDays(10)); // ende aktiv

        assertEquals(SubscriptionTier.PREMIUM, subscriptionService.getEffectiveTier(user));
    }

    // ---- requireTier ----

    @Test
    void requireTier_allows_whenUserHasExactTier() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.PLUS);
        user.setSubscriptionExpiresAt(LocalDate.now().plusDays(10));
        when(userService.getCurrentUser()).thenReturn(user);

        // s'duhet të hedhë exception
        assertDoesNotThrow(() -> subscriptionService.requireTier(SubscriptionTier.PLUS));
    }

    @Test
    void requireTier_allows_whenUserHasHigherTier() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setSubscriptionExpiresAt(LocalDate.now().plusDays(10));
        when(userService.getCurrentUser()).thenReturn(user);

        // PREMIUM duhet të lejohet kur kërkohet PLUS (nivel më i lartë përfshin më të ulëtin)
        assertDoesNotThrow(() -> subscriptionService.requireTier(SubscriptionTier.PLUS));
    }

    @Test
    void requireTier_throws_whenUserHasLowerTier() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.FREE);
        when(userService.getCurrentUser()).thenReturn(user);

        // FREE s'duhet të lejohet kur kërkohet PREMIUM
        assertThrows(RuntimeException.class,
                () -> subscriptionService.requireTier(SubscriptionTier.PREMIUM));
    }

    @Test
    void requireTier_throws_whenSubscriptionExpired() {
        User user = new User();
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setSubscriptionExpiresAt(LocalDate.now().minusDays(1)); // skadoi
        when(userService.getCurrentUser()).thenReturn(user);

        // edhe pse tier=PREMIUM, ka skaduar → efektivisht FREE → duhet të hedhë
        assertThrows(RuntimeException.class,
                () -> subscriptionService.requireTier(SubscriptionTier.PREMIUM));
    }
}