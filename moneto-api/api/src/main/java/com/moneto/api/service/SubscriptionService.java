package com.moneto.api.service;

import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.model.User;
import com.moneto.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final UserService userService;

    public Map<String, Object> getSubscriptionStatus() {
        User user = userService.getCurrentUser();
        SubscriptionTier effectiveTier = getEffectiveTier(user);

        Map<String, Object> status = new HashMap<>();
        status.put("tier", effectiveTier.name());
        status.put("expiresAt", user.getSubscriptionExpiresAt());
        status.put("isActive", effectiveTier != SubscriptionTier.FREE);
        return status;
    }

    public Map<String, Object> subscribe(SubscriptionTier tier) {
        User user = userService.getCurrentUser();

        LocalDate newExpiry;
        if (getEffectiveTier(user) != SubscriptionTier.FREE
                && user.getSubscriptionExpiresAt() != null
                && user.getSubscriptionExpiresAt().isAfter(LocalDate.now())
                && user.getSubscriptionTier() == tier) {
            newExpiry = user.getSubscriptionExpiresAt().plusMonths(1);
        } else {
            newExpiry = LocalDate.now().plusMonths(1);
        }

        user.setSubscriptionTier(tier);
        user.setSubscriptionExpiresAt(newExpiry);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("tier", tier.name());
        result.put("expiresAt", newExpiry);
        result.put("message", "Subscription activated successfully");
        return result;
    }

    public void cancelSubscription() {
        User user = userService.getCurrentUser();
        user.setSubscriptionTier(SubscriptionTier.FREE);
        user.setSubscriptionExpiresAt(null);
        userRepository.save(user);
    }

    // Kthen nivelin efektiv duke marrë parasysh skadimin
    public SubscriptionTier getEffectiveTier(User user) {
        if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
            return SubscriptionTier.FREE;
        }
        if (user.getSubscriptionExpiresAt() == null
                || user.getSubscriptionExpiresAt().isBefore(LocalDate.now())) {
            return SubscriptionTier.FREE;
        }
        return user.getSubscriptionTier();
    }

    // Kontrollon nëse përdoruesi ka të paktën nivelin e kërkuar
    public void requireTier(SubscriptionTier required) {
        User user = userService.getCurrentUser();
        SubscriptionTier current = getEffectiveTier(user);

        if (current.ordinal() < required.ordinal()) {
            throw new RuntimeException("This feature requires a " + required.name() + " subscription");
        }
    }
}