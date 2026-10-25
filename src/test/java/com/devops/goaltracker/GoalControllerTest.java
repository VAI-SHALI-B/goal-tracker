package com.devops.goaltracker;

import com.devops.goaltracker.model.Goal;
import com.devops.goaltracker.repository.GoalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        goalRepository.deleteAll();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("healthy")))
                .andExpect(jsonPath("$.student_id", is("24MIS0378")))
                .andExpect(jsonPath("$.app", is("Goal Tracker")))
                .andExpect(jsonPath("$.version", is("v2")));
    }

    @Test
    void testGetAllGoalsInitiallyEmpty() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.goals", hasSize(0)));
    }

    @Test
    void testCreateGoalSuccess() throws Exception {
        Goal newGoal = new Goal("Master Kubernetes on Windows", "Build CI/CD pipelines", "Academic", "2026-11-30");

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGoal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Goal created successfully")))
                .andExpect(jsonPath("$.goal.id").isNumber())
                .andExpect(jsonPath("$.goal.title", is("Master Kubernetes on Windows")))
                .andExpect(jsonPath("$.goal.category", is("Academic")))
                .andExpect(jsonPath("$.goal.status", is("Pending")));

        // Verify goal is returned in list
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.goals[0].title", is("Master Kubernetes on Windows")));
    }

    @Test
    void testCreateGoalMissingTitle() throws Exception {
        Goal invalidGoal = new Goal("", "Description without title", "General", "2026-12-31");

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGoal)))
                .andExpect(status().isBadRequest());
    }

    // v2 Tests: Update / Modify Goal
    @Test
    void testUpdateGoalSuccess() throws Exception {
        Goal saved = goalRepository.save(new Goal("Initial Goal", "Initial Description", "Personal", "2026-10-31"));

        Goal updateData = new Goal();
        updateData.setTitle("Updated Goal Title");
        updateData.setDescription("Updated description with more details");
        updateData.setCategory("Career");
        updateData.setTargetDate("2026-12-25");
        updateData.setStatus("In Progress");
        updateData.setProgressPct(50);

        mockMvc.perform(put("/api/goals/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Goal updated successfully")))
                .andExpect(jsonPath("$.version", is("v2")))
                .andExpect(jsonPath("$.goal.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.goal.title", is("Updated Goal Title")))
                .andExpect(jsonPath("$.goal.description", is("Updated description with more details")))
                .andExpect(jsonPath("$.goal.category", is("Career")))
                .andExpect(jsonPath("$.goal.targetDate", is("2026-12-25")))
                .andExpect(jsonPath("$.goal.status", is("In Progress")))
                .andExpect(jsonPath("$.goal.progressPct", is(50)));
    }

    @Test
    void testUpdateGoalNotFound() throws Exception {
        Goal updateData = new Goal();
        updateData.setTitle("Does Not Exist");

        mockMvc.perform(put("/api/goals/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("999999 not found")));
    }

    @Test
    void testUpdateGoalBlankTitleReturnsBadRequest() throws Exception {
        Goal saved = goalRepository.save(new Goal("Valid Goal", "Some description", "Health", "2026-12-01"));

        Goal updateData = new Goal();
        updateData.setTitle("   ");

        mockMvc.perform(put("/api/goals/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("cannot be blank")));
    }
}
