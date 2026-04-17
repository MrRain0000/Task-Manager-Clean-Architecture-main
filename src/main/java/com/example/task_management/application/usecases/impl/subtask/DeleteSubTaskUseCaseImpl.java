package com.example.task_management.application.usecases.impl.subtask;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.activitylog.ActivityLogCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.application.usecases.subtask.DeleteSubTaskUseCase;
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
public class DeleteSubTaskUseCaseImpl implements DeleteSubTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteSubTaskUseCaseImpl.class);

    private final SubTaskCommandRepository subTaskCommandRepository;
    private final SubTaskQueryRepository subTaskQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogCommandRepository activityLogCommandRepository;

    public DeleteSubTaskUseCaseImpl(
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
    public void deleteSubTask(Long subtaskId, String userEmail) {
        log.info("[DeleteSubTask] Bắt đầu - subtaskId={}, user={}", subtaskId, userEmail);

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

        String subtaskTitle = subTask.getTitle();
        Long taskId = subTask.getTaskId();
        Long projectId = task.getProjectId();

        // 4. Xóa sub-task
        subTaskCommandRepository.deleteById(subtaskId);
        log.info("[DeleteSubTask] Thành công - subtaskId={}", subtaskId);

        // 5. Reorder các sub-task còn lại
        reorderRemainingSubTasks(taskId);

        // 6. Ghi log hoạt động
        ActivityLog logEntry = ActivityLog.builder()
                .projectId(projectId)
                .userId(user.getId())
                .actionType(ActionType.SUBTASK_DELETED)
                .entityType(EntityType.TASK)
                .entityId(taskId)
                .description("Xóa sub-task: " + subtaskTitle)
                .build();
        activityLogCommandRepository.save(logEntry);
    }

    private void validateProjectMembership(Long projectId, Long userId) {
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của project này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa được chấp nhận vào project này");
        }
    }

    private void reorderRemainingSubTasks(Long taskId) {
        var remainingSubtasks = subTaskQueryRepository.findAllByTaskIdOrderByPositionAsc(taskId);
        for (int i = 0; i < remainingSubtasks.size(); i++) {
            final int position = i;
            var subtask = remainingSubtasks.get(i);
            subtask.setPosition(position);
            subTaskCommandRepository.save(subtask);
        }
    }
}
