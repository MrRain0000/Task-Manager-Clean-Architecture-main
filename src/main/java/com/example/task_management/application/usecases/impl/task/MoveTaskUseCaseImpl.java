package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.dto.request.task.MoveTaskRequest;
import com.example.task_management.application.dto.response.task.TaskResponse;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.task.TaskCommandRepository;
import com.example.task_management.application.repositories.task.TaskQueryRepository;
import com.example.task_management.application.usecases.task.MoveTaskUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.domain.services.TaskOrderService;
import com.example.task_management.interfaces.mappers.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MoveTaskUseCaseImpl implements MoveTaskUseCase {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskQueryRepository taskQueryRepository;
    private final TaskCommandRepository taskCommandRepository;
    private final TaskOrderService taskOrderService;
    private final TaskMapper taskMapper;

    public MoveTaskUseCaseImpl(
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            TaskQueryRepository taskQueryRepository,
            TaskCommandRepository taskCommandRepository,
            TaskOrderService taskOrderService,
            TaskMapper taskMapper) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskQueryRepository = taskQueryRepository;
        this.taskCommandRepository = taskCommandRepository;
        this.taskOrderService = taskOrderService;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public TaskResponse moveTask(Long projectId, Long taskId, MoveTaskRequest request, String userEmail) {

        // Lấy task
        Task task = taskQueryRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task không tồn tại"));

        // Parse status
        TaskStatus toStatus = parseStatus(request.getToStatus());

        // Validate
        task.validateMove(toStatus, request.getToPosition(), projectId);
        validateUserPermission(projectId, userEmail);

        // Thực hiện move qua TaskOrderService
        List<Task> tasksToUpdate = executeMove(projectId, task, toStatus, request.getToPosition());

        // Lưu các task affected
        if (!tasksToUpdate.isEmpty()) {
            taskCommandRepository.saveAll(tasksToUpdate);
        }
        Task savedTask = taskCommandRepository.save(task);

        return taskMapper.toTaskResponse(savedTask);
    }

    private TaskStatus parseStatus(String statusStr) {
        try {
            return TaskStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + statusStr);
        }
    }

    private void validateUserPermission(Long projectId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của dự án này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa chấp nhận lời mời vào dự án này");
        }
    }

    private List<Task> executeMove(Long projectId, Task task, TaskStatus toStatus, Integer toPosition) {
        TaskStatus fromStatus = task.getStatus();
        Integer fromPosition = task.getPosition();

        if (fromStatus.equals(toStatus)) {
            return taskOrderService.moveWithinColumn(projectId, task, fromPosition, toPosition);
        } else {
            return taskOrderService.moveToDifferentColumn(projectId, task, fromStatus, toStatus, fromPosition, toPosition);
        }
    }
}
