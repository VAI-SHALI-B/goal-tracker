package com.devops.goaltracker;

import com.devops.goaltracker.model.Goal;
import com.devops.goaltracker.repository.GoalRepository;
import com.devops.goaltracker.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
