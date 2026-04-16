package com.example.task_management.application.usecases.task;

import com.example.task_management.application.DTOUsecase.response.task.TaskListResult;

// UC24 – Tìm kiếm task theo từ khóa
public interface SearchTasksUseCase {
    TaskListResult searchTasks(Long projectId, String keyword, String userEmail);
}
