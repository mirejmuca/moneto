package com.moneto.api.repository;

import com.moneto.api.model.SavingGoal;
import com.moneto.api.model.SavingGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {
    List<SavingGoal> findByUserId(Long userId);
    List<SavingGoal> findByUserIdAndStatus(Long userId, SavingGoalStatus status);
}