package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectDetailResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.project.GetProjectDetailUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.services.PermissionService;
import com.example.task_management.interfaces.exceptions.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UC20 – Lấy chi tiết project theo ID.
 * Use Case đóng vai trò Orchestrator, tổng hợp thông tin project, members và task summary.
 */
@Service
public class GetProjectDetailUseCaseImpl implements GetProjectDetailUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PermissionService permissionService;

    public GetProjectDetailUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            PermissionService permissionService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResult getProjectDetail(Long projectId, String userEmail) {
        // 1. Validate user là thành viên ACCEPTED của project
        permissionService.validateProjectMember(projectId, userEmail);

        // 2. Lấy project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Dự án không tồn tại"));

        // 3. Lấy danh sách members (chỉ ACCEPTED) và user details
        List<ProjectMember> members = projectMemberRepository.findAllByProjectId(projectId);
        List<ProjectDetailResult.ProjectMemberInfo> memberInfos = members.stream()
                .filter(member -> member.getInvitationStatus() == InvitationStatus.ACCEPTED)
                .map(member -> {
                    User memberUser = userRepository.findById(member.getUserId()).orElse(null);
                    return ProjectDetailResult.ProjectMemberInfo.builder()
                            .userId(member.getUserId())
                            .username(memberUser != null ? memberUser.getUsername() : null)
                            .email(memberUser != null ? memberUser.getEmail() : null)
                            .role(member.getRole())
                            .invitationStatus(member.getInvitationStatus())
                            .build();
                })
                .toList();

        // 4. Lấy task summary (count theo status)
        List<Task> tasks = taskRepository.findAllByProjectId(projectId);
        ProjectDetailResult.TaskSummary taskSummary = buildTaskSummary(tasks);

        // 5. Return kết quả
        return ProjectDetailResult.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .deadline(project.getDeadline())
                .members(memberInfos)
                .taskSummary(taskSummary)
                .build();
    }

    private ProjectDetailResult.TaskSummary buildTaskSummary(List<Task> tasks) {
        int todoCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;
        int cancelledCount = 0;

        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TODO -> todoCount++;
                case IN_PROGRESS -> inProgressCount++;
                case DONE -> doneCount++;
                case CANCELLED -> cancelledCount++;
            }
        }

        return ProjectDetailResult.TaskSummary.builder()
                .totalTasks(tasks.size())
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .cancelledCount(cancelledCount)
                .build();
    }
}
