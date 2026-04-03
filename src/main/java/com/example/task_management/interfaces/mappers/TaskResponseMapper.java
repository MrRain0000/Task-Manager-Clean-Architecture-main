package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.task.TaskDetailResult;
import com.example.task_management.application.DTOUsecase.response.task.TaskResult;
import com.example.task_management.interfaces.dto.response.task.TaskDetailResponse;
import com.example.task_management.interfaces.dto.response.task.TaskResponse;
import org.springframework.stereotype.Component;

@Component
public class TaskResponseMapper {

    public TaskResponse toTaskResponse(TaskResult taskResult) {
        if (taskResult == null) {
            return null;
        }
        return TaskResponse.builder()
                .id(taskResult.getId())
                .title(taskResult.getTitle())
                .description(taskResult.getDescription())
                .status(taskResult.getStatus())
                .projectId(taskResult.getProjectId())
                .assigneeId(taskResult.getAssigneeId())
                .position(taskResult.getPosition())
                .build();
    }

    public TaskDetailResponse toTaskDetailResponse(TaskDetailResult taskDetailResult) {
        if (taskDetailResult == null) {
            return null;
        }

        // Map ProjectInfo
        TaskDetailResponse.ProjectInfoResponse projectInfo = null;
        if (taskDetailResult.getProject() != null) {
            projectInfo = TaskDetailResponse.ProjectInfoResponse.builder()
                    .id(taskDetailResult.getProject().getId())
                    .name(taskDetailResult.getProject().getName())
                    .build();
        }

        // Map AssigneeInfo
        TaskDetailResponse.AssigneeInfoResponse assigneeInfo = null;
        if (taskDetailResult.getAssignee() != null) {
            assigneeInfo = TaskDetailResponse.AssigneeInfoResponse.builder()
                    .id(taskDetailResult.getAssignee().getId())
                    .username(taskDetailResult.getAssignee().getUsername())
                    .email(taskDetailResult.getAssignee().getEmail())
                    .build();
        }

        return TaskDetailResponse.builder()
                .id(taskDetailResult.getId())
                .title(taskDetailResult.getTitle())
                .description(taskDetailResult.getDescription())
                .status(taskDetailResult.getStatus())
                .position(taskDetailResult.getPosition())
                .project(projectInfo)
                .assignee(assigneeInfo)
                .build();
    }
}
