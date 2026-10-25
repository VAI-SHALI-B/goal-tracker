package com.devops.goaltracker.controller;

import com.devops.goaltracker.dto.ProgressReport;
import com.devops.goaltracker.service.GoalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    private final GoalService goalService;

    @Value("${app.version:v3}")
    private String appVersion;

    public ProgressController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * v3 Feature: View - Show goal progress analytics
     */
    @GetMapping
    public ResponseEntity<ProgressReport> getProgress() {
        ProgressReport report = goalService.getProgressReport(appVersion);
        return ResponseEntity.ok(report);
    }
}
