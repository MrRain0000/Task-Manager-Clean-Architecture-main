package com.example.task_management.application.mapper;

import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.domain.entities.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResult toProjectResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResult.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .build();
    }
}
