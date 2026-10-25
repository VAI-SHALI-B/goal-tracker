package com.devops.goaltracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Value("${app.version:v1}")
    private String appVersion;

    @Value("${app.student-id:24MIS0378}")
    private String studentId;

    @Value("${app.name:Goal Tracker}")
    private String appName;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "healthy");
        health.put("version", appVersion);
        health.put("app", appName);
        health.put("student_id", studentId);
        health.put("framework", "Spring Boot 3.3.5");
        health.put("java_version", System.getProperty("java.version"));
        health.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(health);
    }
}
