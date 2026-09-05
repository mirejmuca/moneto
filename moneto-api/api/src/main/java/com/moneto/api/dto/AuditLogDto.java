package com.moneto.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private String userEmail;
    private String action;
    private String details;
    private LocalDateTime createdAt;
}