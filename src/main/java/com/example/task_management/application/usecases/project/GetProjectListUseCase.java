package com.example.task_management.application.usecases.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import java.util.List;

// UC06 – Xem danh sách project
public interface GetProjectListUseCase {
    List<ProjectResult> getMyProjects(String email);
    List<ProjectResult> getProjectsByOwner(String email);
}
