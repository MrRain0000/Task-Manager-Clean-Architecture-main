package com.example.task_management.application.usecases.project;

import com.example.task_management.application.DTOUsecase.request.project.UpdateProjectCommand;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;


public interface UpdateProjectUseCase {
    ProjectResult updateProject(Long projectId, UpdateProjectCommand request, String currentUserEmail);
}
