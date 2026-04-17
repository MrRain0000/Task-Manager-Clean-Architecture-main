package com.example.task_management.application.usecases.impl.subtask;

import com.example.task_management.application.DTOUsecase.request.subtask.CreateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.activitylog.ActivityLogCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.application.usecases.subtask.CreateSubTaskUseCase;
import com.example.task_management.domain.entities.ActivityLog;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateSubTaskUseCaseImpl implements CreateSubTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSubTaskUseCaseImpl.class);

    private final SubTaskCommandRepository subTaskCommandRepository;
    private final SubTaskQueryRepository subTaskQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogCommandRepository activityLogCommandRepository;

    public CreateSubTaskUseCaseImpl(
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
    public SubTaskResult createSubTask(Long taskId, CreateSubTaskRequest request, String userEmail) {
        log.info("[CreateSubTask] Bắt đầu - taskId={}, title={}, user={}", taskId, request.getTitle(), userEmail);

        // 1. Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 2. Validate task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task không tồn tại"));

        // 3. Check quyền (user phải là thành viên ACCEPTED của project)
        validateProjectMembership(task.getProjectId(), user.getId());

        // 4. Validate request
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sub-task không được để trống");
        }
        if (request.getTitle().length() > 200) {
            throw new IllegalArgumentException("Tiêu đề sub-task không được quá 200 ký tự");
        }

        // 5. Validate assignee nếu có
        if (request.getAssigneeId() != null) {
            validateAssignee(task.getProjectId(), request.getAssigneeId());
        }

        // 6. Tính position (append vào cuối)
        Integer maxPosition = subTaskQueryRepository.findMaxPositionByTaskId(taskId);
        int newPosition = (maxPosition == null || maxPosition < 0) ? 0 : maxPosition + 1;

        // 7. Tạo sub-task
        SubTask subTask = new SubTask();
        subTask.setTaskId(taskId);
        subTask.setTitle(request.getTitle().trim());
        subTask.setDescription(request.getDescription());
        subTask.setAssigneeId(request.getAssigneeId());
        subTask.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        subTask.setPosition(newPosition);

        SubTask saved = subTaskCommandRepository.save(subTask);
        log.info("[CreateSubTask] Thành công - subtaskId={}, position={}", saved.getId(), newPosition);

        // 8. Ghi log hoạt động
        ActivityLog logEntry = ActivityLog.builder()
                .projectId(task.getProjectId())
                .userId(user.getId())
                .actionType(ActionType.SUBTASK_CREATED)
                .entityType(EntityType.TASK)
                .entityId(taskId)
                .description("Tạo sub-task: " + saved.getTitle())
                .build();
        activityLogCommandRepository.save(logEntry);

        // 9. Build result
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
