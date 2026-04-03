package com.example.task_management.application.DTOUsecase.response.task;

import com.example.task_management.domain.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin chi tiết của Task bao gồm:
 * - Thông tin task cơ bản
 * - Thông tin người được assign
 * - Thông tin project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResult {

    // --- Task basic info ---
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer position;

    // --- Project info ---
    private ProjectInfoResult project;

    // --- Assignee info ---
    private AssigneeInfoResult assignee;

    /**
     * Thông tin tóm tắt của Project
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfoResult {
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
    public static class AssigneeInfoResult {
        private Long id;
        private String username;
        private String email;
    }
}
