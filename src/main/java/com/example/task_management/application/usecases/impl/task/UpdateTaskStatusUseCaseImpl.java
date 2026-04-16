package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.DTOUsecase.request.task.UpdateTaskStatusCommand;
import com.example.task_management.application.DTOUsecase.response.task.TaskResult;
import com.example.task_management.application.events.TaskStatusUpdatedEvent;
import com.example.task_management.application.mapper.TaskMapper;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.domain.services.PermissionService;
import com.example.task_management.domain.services.Task.TaskStatusTransitionService;
import com.example.task_management.application.usecases.task.UpdateTaskStatusUseCase;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.interfaces.exceptions.TaskNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC14 – Cập nhật trạng thái task.
 * Use Case đóng vai trò Orchestrator, chuyển đổi trạng thái qua domain methods.
 */
@Service
public class UpdateTaskStatusUseCaseImpl implements UpdateTaskStatusUseCase {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskStatusTransitionService transitionService;
    private final PermissionService permissionService;

    public UpdateTaskStatusUseCaseImpl(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            ApplicationEventPublisher eventPublisher,
            TaskStatusTransitionService transitionService,
            PermissionService permissionService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.eventPublisher = eventPublisher;
        this.transitionService = transitionService;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public TaskResult updateTaskStatus(Long taskId, Long projectId, UpdateTaskStatusCommand command, String userEmail) {
        // 1. Validate: User phải tồn tại
        User user = permissionService.validateProjectMember(projectId, userEmail);

        // 2. Validate: Task phải tồn tại và thuộc project
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task không tồn tại"));

        if (!task.belongsToProject(projectId)) {
            throw new TaskNotFoundException("Task không thuộc dự án này");
        }


        // 4. Lưu giá trị cũ trước khi chuyển đổi
        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = command.getStatus();

        // 5. Chuyển đổi trạng thái thông qua domain methods (bảo vệ business rules)
        if (oldStatus != newStatus) {
            transitionService.transitionTo(task, newStatus);
        }

        // 6. Lưu task
        Task updatedTask = taskRepository.save(task);

        // 7. Publish event (decoupled logging)
        eventPublisher.publishEvent(new TaskStatusUpdatedEvent(
                updatedTask.getId(),
                projectId,
                user.getId(),
                updatedTask.getStatus(),
                oldStatus,
                updatedTask.getTitle()
        ));

        // 8. Return kết quả
        return taskMapper.toTaskResult(updatedTask);
    }
}
