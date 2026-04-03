package com.example.task_management.application.usecases.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectListResult;

// UC06 – Xem danh sách project
public interface GetProjectListUseCase {
    ProjectListResult getMyProjects(String email);
    ProjectListResult getProjectsByOwner(String email);
}
