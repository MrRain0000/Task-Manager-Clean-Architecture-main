package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.dashboard.DashboardStatsResult;
import com.example.task_management.interfaces.dto.response.dashboard.DashboardStatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper chuyển đổi giữa DashboardStatsResult (Application) và DashboardStatsResponse (Interface)
 */
@Component
public class DashboardStatsMapper {

    public DashboardStatsResponse toResponse(DashboardStatsResult result) {
        if (result == null) {
            return null;
        }

        return DashboardStatsResponse.builder()
                .totalProjects(result.getTotalProjects())
                .totalTasks(result.getTotalTasks())
                .taskSummary(buildTaskSummary(result.getTaskSummary()))
                .recentCompletedTasks(result.getRecentCompletedTasks())
                .activeTasks(result.getActiveTasks())
                .pendingInvitations(result.getPendingInvitations())
                .weeklyVelocity(buildWeeklyVelocity(result.getWeeklyVelocity()))
                .topProjects(buildTopProjects(result.getTopProjects()))
                .build();
    }

    private DashboardStatsResponse.TaskSummary buildTaskSummary(DashboardStatsResult.TaskSummary summary) {
        if (summary == null) {
            return null;
        }
        return DashboardStatsResponse.TaskSummary.builder()
                .todoCount(summary.getTodoCount())
                .inProgressCount(summary.getInProgressCount())
                .doneCount(summary.getDoneCount())
                .cancelledCount(summary.getCancelledCount())
                .build();
    }

    private List<DashboardStatsResponse.DailyVelocity> buildWeeklyVelocity(
            List<DashboardStatsResult.DailyVelocity> velocities) {
        if (velocities == null) {
            return null;
        }
        return velocities.stream()
                .map(v -> DashboardStatsResponse.DailyVelocity.builder()
                        .date(v.getDate())
                        .completedCount(v.getCompletedCount())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DashboardStatsResponse.TopProject> buildTopProjects(
            List<DashboardStatsResult.TopProject> projects) {
        if (projects == null) {
            return null;
        }
        return projects.stream()
                .map(p -> DashboardStatsResponse.TopProject.builder()
                        .projectId(p.getProjectId())
                        .projectName(p.getProjectName())
                        .totalTasks(p.getTotalTasks())
                        .doneCount(p.getDoneCount())
                        .build())
                .collect(Collectors.toList());
    }
}
