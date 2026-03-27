package com.example.task_management.application.repositories;

import com.example.task_management.application.repositories.task.TaskCommandRepository;
import com.example.task_management.application.repositories.task.TaskQueryRepository;

/**
 * TaskRepository kế thừa cả Query và Command repositories.
 * Có thể dùng trực tiếp hoặc inject riêng TaskQueryRepository/TaskCommandRepository.
 */
public interface TaskRepository extends TaskQueryRepository, TaskCommandRepository {
}
