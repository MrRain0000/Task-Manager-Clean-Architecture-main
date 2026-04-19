package com.example.task_management.application.DTOUsecase.response.project;

import com.example.task_management.domain.enums.MemberRole;
import com.example.task_management.domain.enums.InvitationStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResult {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDate deadline;
    private List<ProjectMemberInfo> members;
    private TaskSummary taskSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectMemberInfo {
        private Long userId;
        private String username;
        private String email;
        private MemberRole role;
        private InvitationStatus invitationStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        private int totalTasks;
        private int todoCount;
        private int inProgressCount;
        private int doneCount;
        private int cancelledCount;
    }
}
