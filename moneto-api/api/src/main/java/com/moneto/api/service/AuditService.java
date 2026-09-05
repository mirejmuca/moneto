package com.moneto.api.service;

import com.moneto.api.dto.AuditLogDto;
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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    // Veprimet, detajet e të cilave janë të ndjeshme dhe duhen maskuar për stafin
    private static final Set<String> SENSITIVE_ACTIONS = Set.of(
            "CREATE_TRANSACTION", "DELETE_TRANSACTION", "SUBSCRIBE"
    );

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

    public Page<AuditLogDto> getLogs(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MODERATOR) {
            throw new RuntimeException("Access denied: staff only");
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toStaffDto);
    }

    public Page<AuditLogDto> getUserLogs(String targetEmail, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MODERATOR) {
            throw new RuntimeException("Access denied: staff only");
        }
        return auditLogRepository.findByUserEmailOrderByCreatedAtDesc(targetEmail, PageRequest.of(page, size))
                .map(this::toStaffDto);
    }

    public Page<AuditLogDto> getMyActivity(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return auditLogRepository.findByUserEmailOrderByCreatedAtDesc(email, PageRequest.of(page, size))
                .map(this::toFullDto);
    }


    private AuditLogDto toStaffDto(AuditLog log) {
        String details = log.getDetails();
        // Për veprimet e ndjeshme, hiq detajet (p.sh. shumat) për stafin
        if (SENSITIVE_ACTIONS.contains(log.getAction())) {
            details = null;
        }
        return new AuditLogDto(log.getId(), log.getUserEmail(), log.getAction(), details, log.getCreatedAt());
    }

    private AuditLogDto toFullDto(AuditLog log) {
        return new AuditLogDto(log.getId(), log.getUserEmail(), log.getAction(), log.getDetails(), log.getCreatedAt());
    }
}