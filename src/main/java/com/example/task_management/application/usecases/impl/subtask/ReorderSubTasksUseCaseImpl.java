package com.example.task_management.application.usecases.impl.subtask;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.subtask.SubTaskCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.application.usecases.subtask.ReorderSubTasksUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReorderSubTasksUseCaseImpl implements ReorderSubTasksUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReorderSubTasksUseCaseImpl.class);

    private final SubTaskCommandRepository subTaskCommandRepository;
    private final SubTaskQueryRepository subTaskQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ReorderSubTasksUseCaseImpl(
            SubTaskCommandRepository subTaskCommandRepository,
            SubTaskQueryRepository subTaskQueryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {
        this.subTaskCommandRepository = subTaskCommandRepository;
        this.subTaskQueryRepository = subTaskQueryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public void reorderSubTasks(Long taskId, List<Long> subtaskIds, String userEmail) {
        log.info("[ReorderSubTasks] Bắt đầu - taskId={}, subtaskIds={}, user={}", taskId, subtaskIds, userEmail);

        // 1. Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 2. Validate task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task không tồn tại"));

        // 3. Check quyền
        validateProjectMembership(task.getProjectId(), user.getId());

        // 4. Validate subtaskIds
        List<SubTask> existingSubtasks = subTaskQueryRepository.findAllByTaskId(taskId);
        Set<Long> existingIds = existingSubtasks.stream()
                .map(SubTask::getId)
                .collect(Collectors.toSet());
        Set<Long> providedIds = new HashSet<>(subtaskIds);

        if (!existingIds.equals(providedIds)) {
            throw new IllegalArgumentException("Danh sách subtaskIds không khớp với các sub-task hiện có");
        }

        // 5. Reorder
        subTaskCommandRepository.reorderSubTasks(taskId, subtaskIds);

        log.info("[ReorderSubTasks] Thành công - taskId={}", taskId);
    }

    private void validateProjectMembership(Long projectId, Long userId) {
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của project này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa được chấp nhận vào project này");
        }
    }
}
