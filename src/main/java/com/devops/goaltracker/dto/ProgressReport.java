package com.devops.goaltracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ProgressReport {

    private String version;

    @JsonProperty("total_goals")
    private long totalGoals;

    @JsonProperty("completed_goals")
    private long completedGoals;

    @JsonProperty("in_progress_goals")
    private long inProgressGoals;

    @JsonProperty("pending_goals")
    private long pendingGoals;

    @JsonProperty("overall_progress_pct")
    private double overallProgressPct;

    @JsonProperty("category_summary")
    private Map<String, CategoryMetric> categorySummary;

    public ProgressReport() {
    }

    public ProgressReport(String version, long totalGoals, long completedGoals, long inProgressGoals,
                          long pendingGoals, double overallProgressPct, Map<String, CategoryMetric> categorySummary) {
        this.version = version;
        this.totalGoals = totalGoals;
        this.completedGoals = completedGoals;
        this.inProgressGoals = inProgressGoals;
        this.pendingGoals = pendingGoals;
        this.overallProgressPct = overallProgressPct;
        this.categorySummary = categorySummary;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTotalGoals() {
        return totalGoals;
    }

    public void setTotalGoals(long totalGoals) {
        this.totalGoals = totalGoals;
    }

    public long getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(long completedGoals) {
        this.completedGoals = completedGoals;
    }

    public long getInProgressGoals() {
        return inProgressGoals;
    }

    public void setInProgressGoals(long inProgressGoals) {
        this.inProgressGoals = inProgressGoals;
    }

    public long getPendingGoals() {
        return pendingGoals;
    }

    public void setPendingGoals(long pendingGoals) {
        this.pendingGoals = pendingGoals;
    }

    public double getOverallProgressPct() {
        return overallProgressPct;
    }

    public void setOverallProgressPct(double overallProgressPct) {
        this.overallProgressPct = overallProgressPct;
    }

    public Map<String, CategoryMetric> getCategorySummary() {
        return categorySummary;
    }

    public void setCategorySummary(Map<String, CategoryMetric> categorySummary) {
        this.categorySummary = categorySummary;
    }

    public static class CategoryMetric {
        private long total;
        private long completed;

        public CategoryMetric() {
        }

        public CategoryMetric(long total, long completed) {
            this.total = total;
            this.completed = completed;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getCompleted() {
            return completed;
        }

        public void setCompleted(long completed) {
            this.completed = completed;
        }

        public void incrementTotal() {
            this.total++;
        }

        public void incrementCompleted() {
            this.completed++;
        }
    }
}
