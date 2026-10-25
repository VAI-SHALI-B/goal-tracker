package com.devops.goaltracker.repository;

import com.devops.goaltracker.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByStatus(String status);
    List<Goal> findByCategory(String category);
}
