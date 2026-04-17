package com.example.task_management.interfaces.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO Response cho Dashboard Statistics API (Interface Layer)
 */
@Data
@Builder
public class DashboardStatsResponse {
    private int totalProjects;
    private int totalTasks;
    private TaskSummary taskSummary;
    private int recentCompletedTasks;
    private int activeTasks;
    private int pendingInvitations;
    private List<DailyVelocity> weeklyVelocity;
    private List<TopProject> topProjects;

    @Data
    @Builder
    public static class TaskSummary {
        private int todoCount;
        private int inProgressCount;
        private int doneCount;
        private int cancelledCount;
    }

    @Data
    @Builder
    public static class DailyVelocity {
        private String date;
        private int completedCount;
    }

    @Data
    @Builder
    public static class TopProject {
        private Long projectId;
        private String projectName;
        private int totalTasks;
        private int doneCount;
    }
}
