package com.devops.goaltracker.controller;

import com.devops.goaltracker.model.Goal;
import com.devops.goaltracker.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*")
public class GoalController {

    private final GoalService goalService;

    @Value("${app.version:v2}")
    private String appVersion;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllGoals() {
        List<Goal> goals = goalService.getAllGoals();
        Map<String, Object> response = new HashMap<>();
        response.put("version", appVersion);
        response.put("count", goals.size());
        response.put("goals", goals);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id) {
        return goalService.getGoalById(id)
                .<ResponseEntity<?>>map(goal -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("version", appVersion);
                    res.put("goal", goal);
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    Map<String, String> err = new HashMap<>();
                    err.put("error", "Goal with ID " + id + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
                });
    }

    /**
     * v1 Feature: Add - Create goal
     */
    @PostMapping
    public ResponseEntity<?> createGoal(@Valid @RequestBody Goal goal) {
        try {
            Goal saved = goalService.createGoal(goal);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal created successfully");
            response.put("version", appVersion);
            response.put("goal", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * v2 Feature: Update - Modify goal
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody Goal updateRequest) {
        try {
            Goal updated = goalService.updateGoal(id, updateRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal updated successfully");
            response.put("version", appVersion);
            response.put("goal", updated);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
