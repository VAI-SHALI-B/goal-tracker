package com.devops.goaltracker;

import com.devops.goaltracker.dto.ProgressReport;
import com.devops.goaltracker.model.Goal;
import com.devops.goaltracker.repository.GoalRepository;
import com.devops.goaltracker.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GoalServiceTest {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalRepository goalRepository;

    @BeforeEach
    void setUp() {
        goalRepository.deleteAll();
    }

    // v1 Tests
    @Test
    void testCreateGoal() {
        Goal goal = new Goal("Learn Spring Boot 3", "Study microservices architecture", "Career", "2026-11-20");
        Goal created = goalService.createGoal(goal);

        assertNotNull(created.getId());
        assertEquals("Learn Spring Boot 3", created.getTitle());
        assertEquals("Career", created.getCategory());
        assertEquals("Pending", created.getStatus());
        assertEquals(1, goalService.getGoalCount());
    }

    @Test
    void testCreateGoalNullTitleThrowsException() {
        Goal goal = new Goal(null, "No title", "Personal", "2026-12-01");
        assertThrows(IllegalArgumentException.class, () -> goalService.createGoal(goal));
    }

    // v2 Tests
    @Test
    void testUpdateGoalSuccess() {
        Goal initial = goalService.createGoal(new Goal("Finish DevOps Assignment", "Build CI/CD pipeline", "Academic", "2026-11-01"));
        assertNotNull(initial.getId());
        assertEquals("Pending", initial.getStatus());

        Goal update = new Goal();
        update.setTitle("Finish DevOps Assignment & Deploy to K8s");
        update.setDescription("Complete Jenkins, Docker, and K8s configuration");
        update.setCategory("Academic");
        update.setTargetDate("2026-11-15");
        update.setStatus("In Progress");
        update.setProgressPct(60);

        Goal updated = goalService.updateGoal(initial.getId(), update);

        assertEquals("Finish DevOps Assignment & Deploy to K8s", updated.getTitle());
        assertEquals("Complete Jenkins, Docker, and K8s configuration", updated.getDescription());
        assertEquals("In Progress", updated.getStatus());
        assertEquals(60, updated.getProgressPct());
        assertEquals("2026-11-15", updated.getTargetDate());
    }

    @Test
    void testUpdateGoalNotFoundThrowsException() {
        Goal update = new Goal("Non-existent", "Desc", "Category", "2026-12-31");
        assertThrows(NoSuchElementException.class, () -> goalService.updateGoal(9999L, update));
    }

    @Test
    void testUpdateGoalBlankTitleThrowsException() {
        Goal initial = goalService.createGoal(new Goal("Valid Goal", "Desc", "Category", "2026-11-01"));
        Goal update = new Goal();
        update.setTitle("   ");

        assertThrows(IllegalArgumentException.class, () -> goalService.updateGoal(initial.getId(), update));
    }

    @Test
    void testUpdateGoalAutoSetsStatusWhenCompleted() {
        Goal initial = goalService.createGoal(new Goal("Certification", "Study exam", "Career", "2026-12-01"));
        Goal update = new Goal();
        update.setProgressPct(100);

        Goal updated = goalService.updateGoal(initial.getId(), update);
        assertEquals("Completed", updated.getStatus());
        assertEquals(100, updated.getProgressPct());
    }

    @Test
    void testUpdateGoalPartialUpdatePreservesOtherFields() {
        Goal initial = goalService.createGoal(new Goal("Original Title", "Original Desc", "Career", "2026-11-20"));
        Goal update = new Goal();
        update.setStatus("Completed");

        Goal updated = goalService.updateGoal(initial.getId(), update);
        assertEquals("Original Title", updated.getTitle());
        assertEquals("Original Desc", updated.getDescription());
        assertEquals("Career", updated.getCategory());
        assertEquals("2026-11-20", updated.getTargetDate());
        assertEquals("Completed", updated.getStatus());
        assertEquals(100, updated.getProgressPct());
    }

    // v3 Tests: View Goal Progress Analytics
    @Test
    void testProgressReportWhenEmpty() {
        ProgressReport report = goalService.getProgressReport("v3");
        assertNotNull(report);
        assertEquals("v3", report.getVersion());
        assertEquals(0, report.getTotalGoals());
        assertEquals(0, report.getCompletedGoals());
        assertEquals(0, report.getInProgressGoals());
        assertEquals(0, report.getPendingGoals());
        assertEquals(0.0, report.getOverallProgressPct());
        assertTrue(report.getCategorySummary().isEmpty());
    }

    @Test
    void testProgressReportWithMultipleGoals() {
        // Goal 1: Completed (100%) - Academic
        Goal g1 = new Goal("Complete Docker Lab", "Containerize app", "Academic", "2026-11-01");
        g1.setStatus("Completed");
        g1.setProgressPct(100);
        goalRepository.save(g1);

        // Goal 2: In Progress (50%) - Career
        Goal g2 = new Goal("Learn Kubernetes", "Deploy to kind cluster", "Career", "2026-11-10");
        g2.setStatus("In Progress");
        g2.setProgressPct(50);
        goalRepository.save(g2);

        // Goal 3: Pending (0%) - Academic
        Goal g3 = new Goal("Prepare Presentation", "Slides for final review", "Academic", "2026-11-25");
        g3.setStatus("Pending");
        g3.setProgressPct(0);
        goalRepository.save(g3);

        ProgressReport report = goalService.getProgressReport("v3");
        assertNotNull(report);
        assertEquals("v3", report.getVersion());
        assertEquals(3, report.getTotalGoals());
        assertEquals(1, report.getCompletedGoals());
        assertEquals(1, report.getInProgressGoals());
        assertEquals(1, report.getPendingGoals());

        // (100 + 50 + 0) / 3 = 50.0%
        assertEquals(50.0, report.getOverallProgressPct());

        // Category breakdown: Academic total 2, completed 1; Career total 1, completed 0
        assertTrue(report.getCategorySummary().containsKey("Academic"));
        assertEquals(2, report.getCategorySummary().get("Academic").getTotal());
        assertEquals(1, report.getCategorySummary().get("Academic").getCompleted());

        assertTrue(report.getCategorySummary().containsKey("Career"));
        assertEquals(1, report.getCategorySummary().get("Career").getTotal());
        assertEquals(0, report.getCategorySummary().get("Career").getCompleted());
    }
}
