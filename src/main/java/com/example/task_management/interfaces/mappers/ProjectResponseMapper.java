package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.project.ProjectDetailResult;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.interfaces.dto.response.project.ProjectDetailResponse;
import com.example.task_management.interfaces.dto.response.project.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectResponseMapper {

    public ProjectResponse toProjectResponse(ProjectResult projectResult) {
        if (projectResult == null) {
            return null;
        }
        return ProjectResponse.builder()
                .id(projectResult.getId())
                .name(projectResult.getName())
                .description(projectResult.getDescription())
                .ownerId(projectResult.getOwnerId())
                .build();
    }

    public ProjectDetailResponse toProjectDetailResponse(ProjectDetailResult result) {
        if (result == null) {
            return null;
        }

        List<ProjectDetailResponse.ProjectMemberInfo> memberInfos = result.getMembers().stream()
                .map(member -> ProjectDetailResponse.ProjectMemberInfo.builder()
                        .userId(member.getUserId())
                        .username(member.getUsername())
                        .email(member.getEmail())
                        .role(member.getRole())
                        .invitationStatus(member.getInvitationStatus())
                        .build())
                .collect(Collectors.toList());

        ProjectDetailResponse.TaskSummary taskSummary = ProjectDetailResponse.TaskSummary.builder()
                .totalTasks(result.getTaskSummary().getTotalTasks())
                .todoCount(result.getTaskSummary().getTodoCount())
                .inProgressCount(result.getTaskSummary().getInProgressCount())
                .doneCount(result.getTaskSummary().getDoneCount())
                .cancelledCount(result.getTaskSummary().getCancelledCount())
                .build();

        return ProjectDetailResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .description(result.getDescription())
                .ownerId(result.getOwnerId())
                .members(memberInfos)
                .taskSummary(taskSummary)
                .build();
    }
}
