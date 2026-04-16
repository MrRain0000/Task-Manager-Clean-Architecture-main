package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.DTOUsecase.response.task.MoveTaskResponse;
import com.example.task_management.application.DTOUsecase.response.task.TaskResult;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.services.PermissionService;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import java.util.Map;
import com.example.task_management.domain.services.Task.TaskStatusParser;
import com.example.task_management.interfaces.dto.request.task.MoveTaskRequest;
import com.example.task_management.application.repositories.task.TaskCommandRepository;
import com.example.task_management.application.repositories.task.TaskQueryRepository;
import com.example.task_management.application.usecases.task.MoveTaskUseCase;

import com.example.task_management.domain.entities.Task;

import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.domain.services.Task.TaskOrderService;
import com.example.task_management.application.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MoveTaskUseCaseImpl implements MoveTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(MoveTaskUseCaseImpl.class);

    private final TaskQueryRepository taskQueryRepository;
    private final TaskCommandRepository taskCommandRepository;
    private final TaskOrderService taskOrderService;
    private final TaskMapper taskMapper;
    private final TaskStatusParser taskStatusParser;
    private final PermissionService permissionService;
    private final LogActivityUseCase logActivityUseCase;

    public MoveTaskUseCaseImpl(TaskQueryRepository taskQueryRepository, TaskCommandRepository taskCommandRepository, TaskOrderService taskOrderService, TaskMapper taskMapper, TaskStatusParser taskStatusParser, PermissionService permissionService, LogActivityUseCase logActivityUseCase) {
        this.taskQueryRepository = taskQueryRepository;
        this.taskCommandRepository = taskCommandRepository;
        this.taskOrderService = taskOrderService;
        this.taskMapper = taskMapper;
        this.taskStatusParser = taskStatusParser;
        this.permissionService = permissionService;
        this.logActivityUseCase = logActivityUseCase;
    }

    @Override
    @Transactional
    public MoveTaskResponse moveTask(Long projectId, Long taskId, MoveTaskRequest request, String userEmail) {
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
        TaskStatus toStatus = taskStatusParser.parseStatus(request.getToStatus());
        log.debug("[MoveTask] Parsed toStatus={}", toStatus);

        // Validate
        log.debug("[MoveTask] Validating move...");
        task.validateMove(toStatus, request.getToPosition(), projectId);

        User user = permissionService.validateProjectMember(projectId, userEmail);
        log.debug("[MoveTask] Validation OK userId={}", user.getId());

        // Lưu giá trị cũ trước khi thực hiện move
        TaskStatus fromStatus = task.getStatus();
        Integer fromPosition = task.getPosition();

        // Kiểm tra có phải same column không
        boolean isSameColumn = fromStatus.equals(toStatus);

        // Execute
        log.debug("[MoveTask] Executing move... sameColumn={}", isSameColumn);
        List<Task> tasksToUpdate = executeMove(projectId, task, toStatus, request.getToPosition());

        // Result
        log.debug("[MoveTask] Tasks affected={}", tasksToUpdate.size());
        // Lưu các task affected
        if (!tasksToUpdate.isEmpty()) {
            taskCommandRepository.saveAll(tasksToUpdate);
            log.debug("[MoveTask] Đã lưu {} tasks", tasksToUpdate.size());
        }
        Task savedTask = taskCommandRepository.save(task);
        log.info("[MoveTask] Hoàn thành - taskId={}, newStatus={}, newPosition={}",
                savedTask.getId(), savedTask.getStatus(), savedTask.getPosition());

        // Ghi log hoạt động (async)
        logActivityUseCase.logActivity(LogActivityRequest.builder()
                .projectId(projectId)
                .userId(user.getId())
                .actionType(ActionType.TASK_MOVED)
                .entityType(EntityType.TASK)
                .entityId(taskId)
                .description("Moved task from " + fromStatus + " to " + toStatus)
                .metadata(Map.of(
                        "fromStatus", fromStatus.name(),
                        "toStatus", toStatus.name(),
                        "fromPosition", fromPosition,
                        "toPosition", request.getToPosition()
                ))
                .build());

        // Smart Response: affectedTasks nếu same column, allTasks nếu different column
        if (isSameColumn) {
            log.debug("[MoveTask] Same column move - returning affected tasks only");
            List<Task> affectedTasks = new ArrayList<>(tasksToUpdate);
            affectedTasks.add(savedTask);
            List<TaskResult> affectedResults = affectedTasks.stream()
                    .map(taskMapper::toTaskResult)
                    .toList();
            return MoveTaskResponse.builder()
                    .sameColumn(true)
                    .affectedTasks(affectedResults)
                    .allTasks(null)
                    .build();
        } else {
            log.debug("[MoveTask] Different column move - returning all tasks");
            List<Task> allTasks = taskQueryRepository.findAllByProjectIdOrderByPosition(projectId);
            List<TaskResult> allResults = allTasks.stream()
                    .map(taskMapper::toTaskResult)
                    .toList();
            return MoveTaskResponse.builder()
                    .sameColumn(false)
                    .affectedTasks(null)
                    .allTasks(allResults)
                    .build();
        }
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
