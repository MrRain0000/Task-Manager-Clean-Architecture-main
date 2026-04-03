package com.example.task_management.application.mapper;

import com.example.task_management.application.DTOUsecase.response.task.TaskResult;
import com.example.task_management.domain.entities.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResult toTaskResult(Task task) {
        return TaskResult.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .projectId(task.getProjectId())
                .assigneeId(task.getAssigneeId())
                .position(task.getPosition())
                .build();
    }
}
