package com.moneto.api.repository;

import com.moneto.api.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserId(Long userId);
    List<Alert> findByUserIdAndActiveTrue(Long userId);
}