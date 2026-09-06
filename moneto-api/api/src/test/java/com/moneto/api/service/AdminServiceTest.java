package com.moneto.api.service;

import com.moneto.api.model.Role;
import com.moneto.api.model.SubscriptionTier;
import com.moneto.api.model.User;
import com.moneto.api.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private RecurringTransactionRepository recurringTransactionRepository;
    @Mock private SavingGoalRepository savingGoalRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SubscriptionService subscriptionService;
    @Mock private UserService userService;
    @Mock private AuditService auditService;

    @InjectMocks
    private AdminService adminService;

    private User userWithRole(Long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setEmail("user" + id + "@test.com");
        return u;
    }

    // ---- changeRole (vetëm admin) ----

    @Test
    void changeRole_throws_whenCallerIsNotAdmin() {
        // Thirrësi është MODERATOR → s'duhet të lejohet
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.MODERATOR));

        assertThrows(RuntimeException.class,
                () -> adminService.changeRole(2L, Role.MODERATOR));
    }

    @Test
    void changeRole_throws_whenPromotingToAdmin() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.ADMIN));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithRole(2L, Role.USER)));

        // S'lejohet ngritja në ADMIN
        assertThrows(RuntimeException.class,
                () -> adminService.changeRole(2L, Role.ADMIN));
    }

    @Test
    void changeRole_succeeds_whenAdminPromotesUserToModerator() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.ADMIN));
        User target = userWithRole(2L, Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.changeRole(2L, Role.MODERATOR);

        assertEquals(Role.MODERATOR, target.getRole());
        verify(userRepository).save(target);
    }

    // ---- deleteUser (hierarkia) ----

    @Test
    void deleteUser_throws_whenTargetIsAdmin() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.ADMIN));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithRole(2L, Role.ADMIN)));

        // Askush s'fshin dot një admin
        assertThrows(RuntimeException.class, () -> adminService.deleteUser(2L));
    }

    @Test
    void deleteUser_throws_whenModeratorDeletesModerator() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.MODERATOR));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithRole(2L, Role.MODERATOR)));

        // Moderatori s'fshin dot një moderator tjetër
        assertThrows(RuntimeException.class, () -> adminService.deleteUser(2L));
    }

    @Test
    void deleteUser_succeeds_whenModeratorDeletesRegularUser() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.MODERATOR));
        User target = userWithRole(2L, Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.deleteUser(2L);

        // Moderatori mund të fshijë një USER normal
        verify(userRepository).delete(target);
    }

    // ---- changeSubscription (vetëm admin, vetëm USER) ----

    @Test
    void changeSubscription_throws_whenTargetIsStaff() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.ADMIN));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithRole(2L, Role.MODERATOR)));

        // Stafi s'ka plan pagese
        assertThrows(RuntimeException.class,
                () -> adminService.changeSubscription(2L, SubscriptionTier.PREMIUM));
    }

    @Test
    void changeSubscription_succeeds_whenAdminChangesUserTier() {
        when(userService.getCurrentUser()).thenReturn(userWithRole(1L, Role.ADMIN));
        User target = userWithRole(2L, Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.changeSubscription(2L, SubscriptionTier.PREMIUM);

        assertEquals(SubscriptionTier.PREMIUM, target.getSubscriptionTier());
        assertNotNull(target.getSubscriptionExpiresAt());
        verify(userRepository).save(target);
    }
}