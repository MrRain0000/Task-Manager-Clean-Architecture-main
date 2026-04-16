package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.DTOUsecase.response.task.TaskListResult;
import com.example.task_management.application.DTOUsecase.response.task.TaskResult;
import com.example.task_management.application.mapper.TaskMapper;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.usecases.task.SearchTasksUseCase;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.services.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UC24 – Tìm kiếm task theo từ khóa.
 * Use Case đóng vai trò Orchestrator, tìm kiếm tasks trong project theo keyword.
 */
@Service
public class SearchTasksUseCaseImpl implements SearchTasksUseCase {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final PermissionService permissionService;

    public SearchTasksUseCaseImpl(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            PermissionService permissionService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskListResult searchTasks(Long projectId, String keyword, String userEmail) {
        // 1. Validate user là thành viên ACCEPTED của project
        permissionService.validateProjectMember(projectId, userEmail);

        // 2. Tìm kiếm tasks
        List<Task> tasks = taskRepository.searchByProjectIdAndKeyword(projectId, keyword);

        // 3. Map to results
        List<TaskResult> taskResults = tasks.stream()
                .map(taskMapper::toTaskResult)
                .toList();

        // 4. Return kết quả
        return TaskListResult.builder()
                .tasks(taskResults)
                .totalCount(taskResults.size())
                .build();
    }
}
