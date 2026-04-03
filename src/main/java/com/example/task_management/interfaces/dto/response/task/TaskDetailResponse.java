package com.example.task_management.interfaces.dto.response.task;

import com.example.task_management.domain.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho API lấy chi tiết Task.
 * Bao gồm: thông tin task, project summary và assignee info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResponse {

    // --- Task basic info ---
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer position;

    // --- Project info ---
    private ProjectInfoResponse project;

    // --- Assignee info ---
    private AssigneeInfoResponse assignee;

    /**
     * Thông tin tóm tắt của Project
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfoResponse {
        private Long id;
        private String name;
    }

    /**
     * Thông tin người được assign task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssigneeInfoResponse {
        private Long id;
        private String username;
        private String email;
    }
}
