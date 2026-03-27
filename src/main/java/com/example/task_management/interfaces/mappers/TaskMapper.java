package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.dto.response.task.TaskResponse;
import com.example.task_management.domain.entities.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
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
