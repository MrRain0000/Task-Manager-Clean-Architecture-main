package com.example.task_management.application.DTOUsecase.request.subtask;

import com.example.task_management.domain.enums.TaskPriority;
import lombok.Data;

/**
 * Request DTO cho tạo mới SubTask
 */
@Data
public class CreateSubTaskRequest {
    private String title;
    private String description;
    private Long assigneeId;
    private TaskPriority priority;
}
