package com.devops.goaltracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Goal title is required")
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String category;

    private String targetDate;

    private String status;

    private Integer progressPct;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Goal() {
        // Default constructor for JPA and JSON deserialization: leave fields null so partial updates work
    }

    public Goal(String title, String description, String category, String targetDate) {
        this.title = title;
        this.description = description;
        this.category = (category != null && !category.isBlank()) ? category : "General";
        this.targetDate = (targetDate != null && !targetDate.isBlank()) ? targetDate : "Not Specified";
        this.status = "Pending";
        this.progressPct = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "Pending";
        }
        if (this.progressPct == null) {
            this.progressPct = 0;
        }
        if (this.category == null || this.category.isBlank()) {
            this.category = "General";
        }
        if (this.targetDate == null || this.targetDate.isBlank()) {
            this.targetDate = "Not Specified";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * v2 Feature Helper: Updates fields from another Goal instance with business logic
     */
    public void updateFrom(Goal other) {
        if (other.getTitle() != null) {
            if (other.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Goal title cannot be blank");
            }
            this.title = other.getTitle().trim();
        }
        if (other.getDescription() != null) {
            this.description = other.getDescription().trim();
        }
        if (other.getCategory() != null && !other.getCategory().trim().isEmpty()) {
            this.category = other.getCategory().trim();
        }
        if (other.getTargetDate() != null && !other.getTargetDate().trim().isEmpty()) {
            this.targetDate = other.getTargetDate().trim();
        }
        if (other.getStatus() != null && !other.getStatus().trim().isEmpty()) {
            this.status = other.getStatus().trim();
            if ("Completed".equalsIgnoreCase(this.status)) {
                this.progressPct = 100;
            }
        }
        if (other.getProgressPct() != null) {
            int pct = Math.max(0, Math.min(100, other.getProgressPct()));
            this.progressPct = pct;
            if (pct == 100) {
                this.status = "Completed";
            } else if (pct > 0 && "Pending".equalsIgnoreCase(this.status)) {
                this.status = "In Progress";
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgressPct() {
        return progressPct;
    }

    public void setProgressPct(Integer progressPct) {
        this.progressPct = progressPct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
