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
import java.util.HashMap;
import com.example.task_management.domain.services.Task.TaskStatusParser;
import com.example.task_management.interfaces.dto.request.task.MoveTaskRequest;
import com.example.task_management.application.repositories.task.TaskCommandRepository;
import com.example.task_management.application.repositories.task.TaskQueryRepository;
import com.example.task_management.application.usecases.task.MoveTaskUseCase;

import com.example.task_management.domain.entities.Task;

import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.domain.services.Task.TaskOrderService;
import com.example.task_management.infrastructure.persistence.jparepositories.TaskJpaRepository;
import com.example.task_management.infrastructure.persistence.jpaentities.TaskJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của MoveTaskUseCase với Bulk Update + Incremental Sync.
 *
 * Flow mới:
 * 1. Bulk update positions trực tiếp trong DB (không load entity)
 * 2. Chỉ save task được move
 * 3. Query lại các affected columns
 * 4. Trả về affectedColumns map (không trả toàn bộ project tasks)
 */
@Service
public class MoveTaskUseCaseImpl implements MoveTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(MoveTaskUseCaseImpl.class);

    private final TaskQueryRepository taskQueryRepository;
    private final TaskCommandRepository taskCommandRepository;
    private final TaskOrderService taskOrderService;
    private final TaskStatusParser taskStatusParser;
    private final PermissionService permissionService;
    private final LogActivityUseCase logActivityUseCase;
    private final TaskJpaRepository taskJpaRepository;

    public MoveTaskUseCaseImpl(TaskQueryRepository taskQueryRepository,
                                TaskCommandRepository taskCommandRepository,
                                TaskOrderService taskOrderService,
                                TaskStatusParser taskStatusParser,
                                PermissionService permissionService,
                                LogActivityUseCase logActivityUseCase,
                                TaskJpaRepository taskJpaRepository) {
        this.taskQueryRepository = taskQueryRepository;
        this.taskCommandRepository = taskCommandRepository;
        this.taskOrderService = taskOrderService;
        this.taskStatusParser = taskStatusParser;
        this.permissionService = permissionService;
        this.logActivityUseCase = logActivityUseCase;
        this.taskJpaRepository = taskJpaRepository;
    }

    @Override
    @Transactional
    public MoveTaskResponse moveTask(Long projectId, Long taskId, MoveTaskRequest request, String userEmail) {
        log.info("[MoveTask] Bắt đầu - projectId={}, taskId={}, toStatus={}, toPosition={}",
                projectId, taskId, request.getToStatus(), request.getToPosition());

        // 1. Lấy task
        Task task = taskQueryRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[MoveTask] Task không tồn tại: taskId={}", taskId);
                    return new IllegalArgumentException("Task không tồn tại");
                });
        log.debug("[MoveTask] Tìm thấy task: id={}, status={}, position={}",
                task.getId(), task.getStatus(), task.getPosition());

        // 2. Parse status
        TaskStatus toStatus = taskStatusParser.parseStatus(request.getToStatus());
        log.debug("[MoveTask] Parsed toStatus={}", toStatus);

        // 3. Validate
        log.debug("[MoveTask] Validating move...");
        task.validateMove(toStatus, request.getToPosition(), projectId);

        User user = permissionService.validateProjectMember(projectId, userEmail);
        log.debug("[MoveTask] Validation OK userId={}", user.getId());

        // 4. Lưu giá trị cũ
        TaskStatus fromStatus = task.getStatus();
        Integer fromPosition = task.getPosition();

        // 5. Execute bulk update (không return List<Task> nữa)
        log.debug("[MoveTask] Executing bulk update...");
        executeMove(projectId, task, toStatus, request.getToPosition());

        // 6. Chỉ save task được move
        Task savedTask = taskCommandRepository.save(task);
        log.info("[MoveTask] Hoàn thành - taskId={}, newStatus={}, newPosition={}",
                savedTask.getId(), savedTask.getStatus(), savedTask.getPosition());

        // 7. Ghi log hoạt động (async)
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

        // 8. Query lại affected columns (Incremental Sync)
        log.debug("[MoveTask] Querying affected columns...");
        Map<String, List<TaskResult>> affectedColumns = buildAffectedColumnsResponse(
                projectId, fromStatus, toStatus, fromStatus.equals(toStatus)
        );

        return MoveTaskResponse.builder()
                .affectedColumns(affectedColumns)
                .build();
    }

    /**
     * Thực hiện move - gọi TaskOrderService với bulk update.
     * Không return List<Task> nữa vì bulk update thực hiện trực tiếp trong DB.
     */
    private void executeMove(Long projectId, Task task, TaskStatus toStatus, Integer toPosition) {
        TaskStatus fromStatus = task.getStatus();
        Integer fromPosition = task.getPosition();
        log.debug("[MoveTask] Execute move: from {}:{} → {}:{}", fromStatus, fromPosition, toStatus, toPosition);

        if (fromStatus.equals(toStatus)) {
            log.debug("[MoveTask] Move within same column");
            taskOrderService.moveWithinColumn(projectId, task, fromPosition, toPosition);
        } else {
            log.debug("[MoveTask] Move to different column");
            taskOrderService.moveToDifferentColumn(projectId, task, fromStatus, toStatus, fromPosition, toPosition);
        }
    }

    /**
     * Build affectedColumns response.
     * Chỉ query lại các columns bị ảnh hưởng, không query toàn bộ project.
     */
    private Map<String, List<TaskResult>> buildAffectedColumnsResponse(
            Long projectId, TaskStatus fromStatus, TaskStatus toStatus, boolean isSameColumn) {

        Map<String, List<TaskResult>> result = new HashMap<>();

        if (isSameColumn) {
            // Same column: chỉ cần query lại 1 column
            log.debug("[MoveTask] Same column - querying only {}", fromStatus);
            List<TaskJpaEntity> columnTasks = taskJpaRepository
                    .findByProjectIdAndStatusOrderByPositionAsc(projectId, fromStatus);
            result.put(fromStatus.name(), mapToTaskResults(columnTasks));
        } else {
            // Different columns: query cả source và target
            log.debug("[MoveTask] Different columns - querying {} and {}", fromStatus, toStatus);

            List<TaskJpaEntity> sourceColumn = taskJpaRepository
                    .findByProjectIdAndStatusOrderByPositionAsc(projectId, fromStatus);
            result.put(fromStatus.name(), mapToTaskResults(sourceColumn));

            List<TaskJpaEntity> targetColumn = taskJpaRepository
                    .findByProjectIdAndStatusOrderByPositionAsc(projectId, toStatus);
            result.put(toStatus.name(), mapToTaskResults(targetColumn));
        }

        return result;
    }

    /**
     * Helper: Convert List<TaskJpaEntity> to List<TaskResult>
     */
    private List<TaskResult> mapToTaskResults(List<TaskJpaEntity> entities) {
        return entities.stream()
                .map(this::toTaskResult)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Convert TaskJpaEntity to TaskResult
     */
    private TaskResult toTaskResult(TaskJpaEntity entity) {
        return TaskResult.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .projectId(entity.getProjectId())
                .assigneeId(entity.getAssigneeId())
                .position(entity.getPosition())
                .build();
    }
}
