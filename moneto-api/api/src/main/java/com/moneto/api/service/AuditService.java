package com.moneto.api.service;

import com.moneto.api.model.AuditLog;
import com.moneto.api.model.Role;
import com.moneto.api.model.User;
import com.moneto.api.repository.AuditLogRepository;
import com.moneto.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void log(String userEmail, String action, String details) {
        AuditLog entry = new AuditLog();
        entry.setUserEmail(userEmail);
        entry.setAction(action);
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }

    public void logCurrentUser(String action, String details) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            log(email, action, details);
        } catch (Exception e) {
            // nëse s'ka përdorues të loguar, e anashkalojmë
        }
    }

    public Page<AuditLog> getLogs(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MODERATOR) {
            throw new RuntimeException("Access denied: staff only");
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public Page<AuditLog> getMyActivity(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return auditLogRepository.findByUserEmailOrderByCreatedAtDesc(email, PageRequest.of(page, size));
    }
}