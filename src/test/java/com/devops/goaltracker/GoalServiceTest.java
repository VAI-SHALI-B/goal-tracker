package com.devops.goaltracker;

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
}
