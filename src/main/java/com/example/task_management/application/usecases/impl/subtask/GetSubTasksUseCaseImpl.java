package com.example.task_management.application.usecases.impl.subtask;

import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.application.usecases.subtask.GetSubTasksUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetSubTasksUseCaseImpl implements GetSubTasksUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSubTasksUseCaseImpl.class);

    private final SubTaskQueryRepository subTaskQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public GetSubTasksUseCaseImpl(
            SubTaskQueryRepository subTaskQueryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {
        this.subTaskQueryRepository = subTaskQueryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public List<SubTaskResult> getSubTasksByTaskId(Long taskId, String userEmail) {
        log.info("[GetSubTasks] Bắt đầu - taskId={}, user={}", taskId, userEmail);

        // Validate và lấy dữ liệu
        Task task = validateAndGetTask(taskId, userEmail);

        // Lấy danh sách sub-tasks
        List<SubTask> subtasks = subTaskQueryRepository.findAllByTaskIdOrderByPositionAsc(taskId);

        log.info("[GetSubTasks] Thành công - taskId={}, count={}", taskId, subtasks.size());

        return subtasks.stream()
                .map(s -> buildResult(s, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubTaskResult> getSubTasksByTaskIdAndStatus(Long taskId, TaskStatus status, String userEmail) {
        log.info("[GetSubTasksByStatus] Bắt đầu - taskId={}, status={}, user={}", taskId, status, userEmail);

        // Validate và lấy dữ liệu
        Task task = validateAndGetTask(taskId, userEmail);

        // Lấy danh sách sub-tasks theo status
        List<SubTask> subtasks = subTaskQueryRepository.findAllByTaskIdAndStatus(taskId, status);

        // Sort theo position
        subtasks.sort((a, b) -> Integer.compare(a.getPosition(), b.getPosition()));

        log.info("[GetSubTasksByStatus] Thành công - taskId={}, status={}, count={}",
                taskId, status, subtasks.size());

        return subtasks.stream()
                .map(s -> buildResult(s, null))
                .collect(Collectors.toList());
    }

    private Task validateAndGetTask(Long taskId, String userEmail) {
        // 1. Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 2. Validate task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task không tồn tại"));

        // 3. Check quyền
        validateProjectMembership(task.getProjectId(), user.getId());

        return task;
    }

    private void validateProjectMembership(Long projectId, Long userId) {
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của project này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa được chấp nhận vào project này");
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
