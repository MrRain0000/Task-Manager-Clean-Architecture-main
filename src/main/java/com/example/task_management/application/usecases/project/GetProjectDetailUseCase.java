package com.example.task_management.application.usecases.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectDetailResult;

// UC20 – Lấy chi tiết project theo ID
public interface GetProjectDetailUseCase {
    ProjectDetailResult getProjectDetail(Long projectId, String userEmail);
}
