package com.example.task_management.application.usecases.task;

import com.example.task_management.application.dto.request.task.AssignTaskRequest;
import com.example.task_management.application.dto.response.task.TaskResponse;

// UC10 – Gán task
public interface AssignTaskUseCase {
    TaskResponse assignTask(Long projectId, Long taskId, AssignTaskRequest request, String userEmail);
}
