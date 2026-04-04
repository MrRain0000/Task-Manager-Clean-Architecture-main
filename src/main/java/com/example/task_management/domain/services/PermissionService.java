package com.example.task_management.domain.services;

import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.User;

public interface PermissionService {
    Long validateProjectMember(Long projectId, String email);

    boolean canUpdateProject(Project project, User user);
}
