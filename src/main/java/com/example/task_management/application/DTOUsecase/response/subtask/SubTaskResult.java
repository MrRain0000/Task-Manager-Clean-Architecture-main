package com.example.task_management.application.DTOUsecase.response.subtask;

import com.example.task_management.domain.enums.TaskPriority;
import com.example.task_management.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO chứa thông tin SubTask trả về từ use case
 */
@Data
@Builder
public class SubTaskResult {
    private Long id;
    private Long taskId;
    private String title;
    private String description;
    private Long assigneeId;
    private String assigneeName;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
