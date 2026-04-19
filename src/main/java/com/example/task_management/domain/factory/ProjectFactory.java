package com.example.task_management.domain.factory;

import com.example.task_management.domain.entities.Project;
import java.time.LocalDate;

public class ProjectFactory {
    public static Project create(String name, String description, Long ownerId, LocalDate deadline) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name is required");
        }

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwnerId(ownerId);
        project.setDeadline(deadline);

        return project;
    }
}
