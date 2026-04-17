package com.example.task_management.application.usecases.impl.subtask;

import com.example.task_management.application.DTOUsecase.request.subtask.UpdateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.activitylog.ActivityLogCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.application.usecases.subtask.UpdateSubTaskUseCase;
import com.example.task_management.domain.entities.ActivityLog;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import com.example.task_management.domain.enums.InvitationStatus;
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
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogCommandRepository activityLogCommandRepository;

    public UpdateSubTaskUseCaseImpl(
            SubTaskCommandRepository subTaskCommandRepository,
            SubTaskQueryRepository subTaskQueryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            ActivityLogCommandRepository activityLogCommandRepository) {
        this.subTaskCommandRepository = subTaskCommandRepository;
        this.subTaskQueryRepository = subTaskQueryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
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
        validateProjectMembership(task.getProjectId(), user.getId());

        // 4. Validate title nếu có update
        if (request.getTitle() != null) {
            if (request.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Tiêu đề sub-task không được để trống");
            }
            if (request.getTitle().length() > 200) {
                throw new IllegalArgumentException("Tiêu đề sub-task không được quá 200 ký tự");
            }
        }

        // 5. Validate assignee nếu có update
        if (request.getAssigneeId() != null) {
            validateAssignee(task.getProjectId(), request.getAssigneeId());
        }

        // 6. Update sub-task
        subTask.update(
                request.getTitle(),
                request.getDescription(),
                request.getAssigneeId(),
                request.getPriority(),
                request.getStatus()
        );

        SubTask saved = subTaskCommandRepository.save(subTask);
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

    private void validateProjectMembership(Long projectId, Long userId) {
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của project này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa được chấp nhận vào project này");
        }
    }

    private void validateAssignee(Long projectId, Long assigneeId) {
        ProjectMember assigneeMember = projectMemberRepository.findByProjectIdAndUserId(projectId, assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("Người được giao không phải thành viên project"));

        if (assigneeMember.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Người được giao chưa được chấp nhận vào project");
        }
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
                .position(subTask.getPosition())
                .createdAt(subTask.getCreatedAt())
                .updatedAt(subTask.getUpdatedAt())
                .build();
    }
}
