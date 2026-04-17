package com.example.task_management.interfaces.dto.response.subtask;

import com.example.task_management.domain.enums.TaskPriority;
import com.example.task_management.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Response cho Sub-task API (Interface Layer)
 */
@Data
@Builder
public class SubTaskResponse {
    private Long id;
    private Long taskId;
    private String title;
    private String description;
    private AssigneeInfo assignee;
    private TaskPriority priority;
    private TaskStatus status;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AssigneeInfo {
        private Long id;
        private String name;
    }
}
