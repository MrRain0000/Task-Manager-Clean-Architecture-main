package com.example.task_management.application.DTOUsecase.request.subtask;

import com.example.task_management.domain.enums.TaskPriority;
import com.example.task_management.domain.enums.TaskStatus;
import lombok.Data;

/**
 * Request DTO cho cập nhật SubTask
 */
@Data
public class UpdateSubTaskRequest {
    private String title;
    private String description;
    private Long assigneeId;
    private TaskPriority priority;
    private TaskStatus status;
}
