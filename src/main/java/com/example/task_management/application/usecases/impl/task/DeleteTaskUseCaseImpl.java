package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.task.DeleteTaskUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import java.util.Map;
import com.example.task_management.interfaces.exceptions.ProjectAccessDeniedException;
import com.example.task_management.interfaces.exceptions.ProjectNotFoundException;
import com.example.task_management.interfaces.exceptions.TaskNotFoundException;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation của DeleteTaskUseCase.
 * Tuân thủ SRP: Chỉ xử lý logic xóa task.
 */
@Service
public class DeleteTaskUseCaseImpl implements DeleteTaskUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final LogActivityUseCase logActivityUseCase;

    public DeleteTaskUseCaseImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            LogActivityUseCase logActivityUseCase) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.logActivityUseCase = logActivityUseCase;
    }

    @Override
    public void deleteTask(Long projectId, Long taskId, String userEmail) {
        // Rule 1: Validate project tồn tại
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Dự án không tồn tại"));

        // Rule 2: Validate user tồn tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // Rule 3: Validate user là thành viên ACCEPTED của project
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ProjectAccessDeniedException("Bạn không phải thành viên của dự án này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new ProjectAccessDeniedException("Bạn chưa chấp nhận lời mời vào dự án này");
        }

        // Rule 4: Validate task tồn tại và thuộc về project
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task không tồn tại"));

        if (!task.getProjectId().equals(projectId)) {
            throw new TaskNotFoundException("Task không thuộc dự án này");
        }

        // Rule 5: Xóa task
        String taskTitle = task.getTitle();
        taskRepository.deleteById(taskId);

        // Ghi log hoạt động (async)
        logActivityUseCase.logActivity(LogActivityRequest.builder()
                .projectId(projectId)
                .userId(user.getId())
                .actionType(ActionType.TASK_DELETED)
                .entityType(EntityType.TASK)
                .entityId(taskId)
                .description("Deleted task: " + taskTitle)
                .metadata(Map.of("title", taskTitle))
                .build());
    }
}
