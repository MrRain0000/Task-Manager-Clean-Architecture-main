package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.DTOUsecase.response.task.TaskDetailResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.task.GetTaskDetailUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.interfaces.exceptions.ProjectAccessDeniedException;
import com.example.task_management.interfaces.exceptions.ProjectNotFoundException;
import com.example.task_management.interfaces.exceptions.TaskNotFoundException;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation của GetTaskDetailUseCase.
 * Tuân thủ SRP: Chỉ xử lý logic lấy chi tiết task.
 */
@Service
public class GetTaskDetailUseCaseImpl implements GetTaskDetailUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public GetTaskDetailUseCaseImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TaskDetailResult getTaskDetail(Long projectId, Long taskId, String userEmail) {
        // Rule 1: Validate project tồn tại
        Project project = projectRepository.findById(projectId)
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

        // Build ProjectInfo
        TaskDetailResult.ProjectInfoResult projectInfo = TaskDetailResult.ProjectInfoResult.builder()
                .id(project.getId())
                .name(project.getName())
                .build();

        // Build AssigneeInfo nếu có assignee
        TaskDetailResult.AssigneeInfoResult assigneeInfo = null;
        if (task.getAssigneeId() != null) {
            User assignee = userRepository.findById(task.getAssigneeId())
                    .orElse(null);
            if (assignee != null) {
                assigneeInfo = TaskDetailResult.AssigneeInfoResult.builder()
                        .id(assignee.getId())
                        .username(assignee.getUsername())
                        .email(assignee.getEmail())
                        .build();
            }
        }

        // Build và return TaskDetailResult
        return TaskDetailResult.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .position(task.getPosition())
                .project(projectInfo)
                .assignee(assigneeInfo)
                .build();
    }
}
