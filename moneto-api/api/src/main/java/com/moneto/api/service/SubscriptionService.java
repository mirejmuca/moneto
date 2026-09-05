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
    private final AuditService auditService;
    private final EmailService emailService;

    public Map<String, Object> getSubscriptionStatus() {
        User user = userService.getCurrentUser();
        SubscriptionTier effectiveTier = getEffectiveTier(user);

        Map<String, Object> status = new HashMap<>();
        status.put("tier", effectiveTier.name());
        status.put("expiresAt", user.getSubscriptionExpiresAt());
        status.put("isActive", effectiveTier != SubscriptionTier.FREE);
        return status;
    }

    public Map<String, Object> subscribe(SubscriptionTier tier, String cardLast4) {
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

        // Gjenero një ID transaksioni të simuluar
        String transactionId = "txn_" + java.util.UUID.randomUUID().toString().substring(0, 12);

        // Përcakto çmimin sipas planit
        String amount = tier == SubscriptionTier.PREMIUM ? "$4.99" : "$2.99";

        // Dërgo faturën me email (jo-bllokuese — nëse dështon, abonimi mbetet aktiv)
        try {
            emailService.sendInvoiceEmail(
                    user.getEmail(),
                    user.getName(),
                    tier.name(),
                    amount,
                    LocalDate.now(),
                    newExpiry,
                    transactionId,
                    cardLast4 != null ? cardLast4 : "****"
            );
        } catch (Exception e) {
            System.out.println("Warning: invoice email could not be sent: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tier", tier.name());
        result.put("expiresAt", newExpiry);
        result.put("transactionId", transactionId);
        result.put("message", "Subscription activated successfully");

        auditService.logCurrentUser("SUBSCRIBE", "Subscribed to " + tier);
        return result;
    }

    public void cancelSubscription() {
        User user = userService.getCurrentUser();
        user.setSubscriptionTier(SubscriptionTier.FREE);
        user.setSubscriptionExpiresAt(null);
        userRepository.save(user);
        auditService.logCurrentUser("CANCEL_SUBSCRIPTION", null);
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