package com.moneto.api.service;

import com.moneto.api.model.Role;
import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.model.User;
import com.moneto.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final NotificationRepository notificationRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionService subscriptionService;
    private final UserService userService;


    public List<Map<String, Object>> getAllUsers() {
        requireStaff();

        return userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("tier", subscriptionService.getEffectiveTier(u).name());
            map.put("role", u.getRole().name());
            map.put("verified", u.getIsVerified());
            return map;
        }).toList();
    }

    public Map<String, Object> getStats() {
        requireStaff();

        List<User> allUsers = userRepository.findAll();
// Vetëm përdoruesit normalë kanë plane pagese
        List<User> customers = allUsers.stream()
                .filter(u -> u.getRole() == Role.USER)
                .toList();

        long total = customers.size();
        long free = customers.stream().filter(u -> subscriptionService.getEffectiveTier(u) == SubscriptionTier.FREE).count();
        long plus = customers.stream().filter(u -> subscriptionService.getEffectiveTier(u) == SubscriptionTier.PLUS).count();
        long premium = customers.stream().filter(u -> subscriptionService.getEffectiveTier(u) == SubscriptionTier.PREMIUM).count();
        long verified = customers.stream().filter(u -> Boolean.TRUE.equals(u.getIsVerified())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", total);
        stats.put("freeUsers", free);
        stats.put("plusUsers", plus);
        stats.put("premiumUsers", premium);
        stats.put("verifiedUsers", verified);
        return stats;
    }

    public void changeRole(Long userId, Role newRole) {
        requireAdmin(); // vetëm admin cakton role

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mos lejo ndryshimin e rolit të një admini
        if (target.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot change an admin's role");
        }

        // Mos lejo ngritjen e dikujt në ADMIN përmes këtij endpoint-i
        if (newRole == Role.ADMIN) {
            throw new RuntimeException("Cannot promote to admin");
        }

        target.setRole(newRole);

// Stafi (moderator/admin) s'ka plan pagese — hiqe abonimin
        if (newRole == Role.MODERATOR) {
            target.setSubscriptionTier(SubscriptionTier.FREE);
            target.setSubscriptionExpiresAt(null);
        }

        userRepository.save(target);
    }

    private void requireStaff() {
        User user = userService.getCurrentUser();
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MODERATOR) {
            throw new RuntimeException("Access denied: staff only");
        }
    }

    private void requireAdmin() {
        User user = userService.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied: admin only");
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        requireStaff(); // admin ose moderator

        User currentUser = userService.getCurrentUser();
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Askush s'mund të fshijë një admin
        if (target.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete an admin account");
        }

        // Moderatori s'mund të fshijë moderatorë të tjerë (vetëm admin mundet)
        if (target.getRole() == Role.MODERATOR && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only an admin can delete a moderator");
        }

        transactionRepository.deleteAll(transactionRepository.findByUserId(userId));
        budgetRepository.deleteAll(budgetRepository.findByUserId(userId));
        recurringTransactionRepository.deleteAll(recurringTransactionRepository.findByUserId(userId));
        savingGoalRepository.deleteAll(savingGoalRepository.findByUserId(userId));
        notificationRepository.deleteAll(notificationRepository.findByUserId(userId));
        categoryRepository.deleteAll(categoryRepository.findByUserId(userId));
        userRepository.delete(target);
    }
}