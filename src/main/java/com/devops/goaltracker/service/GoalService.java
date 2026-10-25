package com.devops.goaltracker.service;

import com.devops.goaltracker.dto.ProgressReport;
import com.devops.goaltracker.model.Goal;
import com.devops.goaltracker.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * v3 Feature: View - Show goal progress analytics
     */
    public ProgressReport getProgressReport(String version) {
        List<Goal> all = goalRepository.findAll();
        long total = all.size();
        if (total == 0) {
            return new ProgressReport(version, 0, 0, 0, 0, 0.0, Collections.emptyMap());
        }

        long completed = all.stream().filter(g -> "Completed".equalsIgnoreCase(g.getStatus())).count();
        long inProgress = all.stream().filter(g -> "In Progress".equalsIgnoreCase(g.getStatus())).count();
        long pending = all.stream().filter(g -> "Pending".equalsIgnoreCase(g.getStatus())).count();

        double totalPct = all.stream()
                .mapToInt(g -> g.getProgressPct() != null ? g.getProgressPct() : 0)
                .sum();
        double overallPct = Math.round((totalPct / total) * 10.0) / 10.0;

        Map<String, ProgressReport.CategoryMetric> categories = new LinkedHashMap<>();
        for (Goal g : all) {
            String cat = (g.getCategory() != null && !g.getCategory().isBlank()) ? g.getCategory() : "General";
            ProgressReport.CategoryMetric metric = categories.computeIfAbsent(cat, k -> new ProgressReport.CategoryMetric());
            metric.incrementTotal();
            if ("Completed".equalsIgnoreCase(g.getStatus())) {
                metric.incrementCompleted();
            }
        }

        return new ProgressReport(version, total, completed, inProgress, pending, overallPct, categories);
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
