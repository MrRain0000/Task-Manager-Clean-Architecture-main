package com.example.task_management.application.DTOUsecase.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResult {
    private Long id;
    private String username;
    private String email;
    private boolean isVerified;
    private int totalProjects;
    private int totalTasks;
}
