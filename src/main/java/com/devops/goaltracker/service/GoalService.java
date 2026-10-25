package com.devops.goaltracker.service;

import com.devops.goaltracker.model.Goal;
import com.devops.goaltracker.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    /**
     * v1 Feature: Add - Create goal
     */
    public Goal createGoal(Goal goal) {
        if (goal.getTitle() == null || goal.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Goal title is required");
        }
        return goalRepository.save(goal);
    }

    /**
     * v2 Feature: Update - Modify goal
     */
    public Goal updateGoal(Long id, Goal updateData) {
        Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Goal with ID " + id + " not found"));
        existing.updateFrom(updateData);
        return goalRepository.save(existing);
    }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public Optional<Goal> getGoalById(Long id) {
        return goalRepository.findById(id);
    }

    public long getGoalCount() {
        return goalRepository.count();
    }
}
