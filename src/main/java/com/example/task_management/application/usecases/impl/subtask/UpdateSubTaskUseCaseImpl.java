package com.example.task_management.application.usecases.impl.subtask;

import com.example.task_management.application.DTOUsecase.request.subtask.UpdateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.activitylog.ActivityLogCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.application.usecases.subtask.UpdateSubTaskUseCase;
import com.example.task_management.domain.entities.ActivityLog;
import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import com.example.task_management.domain.services.Task.TaskAssignerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateSubTaskUseCaseImpl implements UpdateSubTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateSubTaskUseCaseImpl.class);

    private final SubTaskCommandRepository subTaskCommandRepository;
    private final SubTaskQueryRepository subTaskQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssignerService taskAssignerService;
    private final ActivityLogCommandRepository activityLogCommandRepository;

    public UpdateSubTaskUseCaseImpl(
            SubTaskCommandRepository subTaskCommandRepository,
            SubTaskQueryRepository subTaskQueryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskAssignerService taskAssignerService,
            ActivityLogCommandRepository activityLogCommandRepository) {
        this.subTaskCommandRepository = subTaskCommandRepository;
        this.subTaskQueryRepository = subTaskQueryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskAssignerService = taskAssignerService;
        this.activityLogCommandRepository = activityLogCommandRepository;
    }

    @Override
    public SubTaskResult updateSubTask(Long subtaskId, UpdateSubTaskRequest request, String userEmail) {
        log.info("[UpdateSubTask] Bắt đầu - subtaskId={}, user={}", subtaskId, userEmail);

        // 1. Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 2. Validate sub-task tồn tại
        SubTask subTask = subTaskQueryRepository.findById(subtaskId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-task không tồn tại"));

        // 3. Get task chính và validate quyền
        Task task = taskRepository.findById(subTask.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task chính không tồn tại"));
        taskAssignerService.validateAssignerMembership(task.getProjectId(), user.getId());

        // 4. Validate title nếu có update
        if (request.getTitle() != null) {
            if (request.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Tiêu đề sub-task không được để trống");
            }
            if (request.getTitle().length() > 200) {
                throw new IllegalArgumentException("Tiêu đề sub-task không được quá 200 ký tự");
            }
        }

        // 5. Validate assignee nếu có update (phải là thành viên ACCEPTED)
        if (request.getAssigneeId() != null) {
            taskAssignerService.validateAssignee(task.getProjectId(), request.getAssigneeId());
        }

        // 6. Lưu status, assignee và priority cũ để log
        var oldStatus = subTask.getStatus();
        var oldAssigneeId = subTask.getAssigneeId();
        var oldPriority = subTask.getPriority();
        log.debug("[UpdateSubTask] Status hiện tại: {}, Status request: {}", oldStatus, request.getStatus());
        log.debug("[UpdateSubTask] Assignee hiện tại: {}, Assignee request: {}", oldAssigneeId, request.getAssigneeId());
        log.debug("[UpdateSubTask] Priority hiện tại: {}, Priority request: {}", oldPriority, request.getPriority());

        // 7. Update sub-task
        subTask.update(
                request.getTitle(),
                request.getDescription(),
                request.getAssigneeId(),
                request.getPriority(),
                request.getStatus()
        );

        SubTask saved = subTaskCommandRepository.save(subTask);

        // Log chi tiết status change
        if (request.getStatus() != null && !request.getStatus().equals(oldStatus)) {
            log.info("[UpdateSubTask] Status thay đổi: {} → {} (subtaskId={})",
                    oldStatus, request.getStatus(), saved.getId());
        }
        // Log chi tiết assignee change
        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(oldAssigneeId)) {
            log.info("[UpdateSubTask] Assignee thay đổi: {} → {} (subtaskId={})",
                    oldAssigneeId, request.getAssigneeId(), saved.getId());
        }
        // Log chi tiết priority change
        if (request.getPriority() != null && !request.getPriority().equals(oldPriority)) {
            log.info("[UpdateSubTask] Priority thay đổi: {} → {} (subtaskId={})",
                    oldPriority, request.getPriority(), saved.getId());
        }
        log.info("[UpdateSubTask] Thành công - subtaskId={}", saved.getId());

        // 7. Ghi log hoạt động
        ActivityLog logEntry = ActivityLog.builder()
                .projectId(task.getProjectId())
                .userId(user.getId())
                .actionType(ActionType.SUBTASK_UPDATED)
                .entityType(EntityType.TASK)
                .entityId(subTask.getTaskId())
                .description("Cập nhật sub-task: " + saved.getTitle())
                .build();
        activityLogCommandRepository.save(logEntry);

        // 8. Build result
        return buildResult(saved, null);
    }

    private SubTaskResult buildResult(SubTask subTask, String assigneeName) {
        return SubTaskResult.builder()
                .id(subTask.getId())
                .taskId(subTask.getTaskId())
                .title(subTask.getTitle())
                .description(subTask.getDescription())
                .assigneeId(subTask.getAssigneeId())
                .assigneeName(assigneeName)
                .priority(subTask.getPriority())
                .status(subTask.getStatus())
                .createdAt(subTask.getCreatedAt())
                .updatedAt(subTask.getUpdatedAt())
                .build();
    }
}
