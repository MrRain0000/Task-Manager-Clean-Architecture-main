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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MoveTaskUseCaseImpl implements MoveTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(MoveTaskUseCaseImpl.class);

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
        log.info("[MoveTask] Bắt đầu - projectId={}, taskId={}, toStatus={}, toPosition={}", 
                projectId, taskId, request.getToStatus(), request.getToPosition());

        // Lấy task
        Task task = taskQueryRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[MoveTask] Task không tồn tại: taskId={}", taskId);
                    return new IllegalArgumentException("Task không tồn tại");
                });
        log.debug("[MoveTask] Tìm thấy task: id={}, status={}, position={}", 
                task.getId(), task.getStatus(), task.getPosition());

        // Parse status
        TaskStatus toStatus = parseStatus(request.getToStatus());
        log.debug("[MoveTask] Parsed status: {}", toStatus);

        // Validate
        log.debug("[MoveTask] Validate move...");
        task.validateMove(toStatus, request.getToPosition(), projectId);
        Long userId = validateUserPermission(projectId, userEmail);
        log.debug("[MoveTask] Validation OK, userId={}", userId);

        // Thực hiện move qua TaskOrderService
        log.debug("[MoveTask] Gọi TaskOrderService...");
        List<Task> tasksToUpdate = executeMove(projectId, task, toStatus, request.getToPosition());
        log.debug("[MoveTask] Cần cập nhật {} tasks", tasksToUpdate.size());

        // Lưu các task affected
        if (!tasksToUpdate.isEmpty()) {
            taskCommandRepository.saveAll(tasksToUpdate);
            log.debug("[MoveTask] Đã lưu {} tasks", tasksToUpdate.size());
        }
        Task savedTask = taskCommandRepository.save(task);
        log.info("[MoveTask] Hoàn thành - taskId={}, newStatus={}, newPosition={}", 
                savedTask.getId(), savedTask.getStatus(), savedTask.getPosition());

        return taskMapper.toTaskResponse(savedTask);
    }

    private TaskStatus parseStatus(String statusStr) {
        try {
            return TaskStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            log.error("[MoveTask] Status không hợp lệ: {}", statusStr);
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + statusStr);
        }
    }

    private Long validateUserPermission(Long projectId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("[MoveTask] User không tồn tại");
                    return new IllegalArgumentException("Người dùng không tồn tại");
                });
        log.debug("[MoveTask] User tồn tại: id={}", user.getId());

        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> {
                    log.error("[MoveTask] User không phải thành viên project: userId={}", user.getId());
                    return new IllegalArgumentException("Bạn không phải thành viên của dự án này");
                });

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            log.error("[MoveTask] User chưa ACCEPTED: userId={}, status={}", 
                    user.getId(), membership.getInvitationStatus());
            throw new IllegalArgumentException("Bạn chưa chấp nhận lời mời vào dự án này");
        }
        log.debug("[MoveTask] User permission OK");
        return user.getId();
    }

    private List<Task> executeMove(Long projectId, Task task, TaskStatus toStatus, Integer toPosition) {
        TaskStatus fromStatus = task.getStatus();
        Integer fromPosition = task.getPosition();
        log.debug("[MoveTask] Execute move: from {}:{} → {}:{}", fromStatus, fromPosition, toStatus, toPosition);

        if (fromStatus.equals(toStatus)) {
            log.debug("[MoveTask] Move within same column");
            return taskOrderService.moveWithinColumn(projectId, task, fromPosition, toPosition);
        } else {
            log.debug("[MoveTask] Move to different column");
            return taskOrderService.moveToDifferentColumn(projectId, task, fromStatus, toStatus, fromPosition, toPosition);
        }
    }
}
