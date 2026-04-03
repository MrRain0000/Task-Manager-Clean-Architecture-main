package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.DTOUsecase.response.task.TaskResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.mapper.TaskMapper;
import com.example.task_management.application.usecases.task.GetTaskUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GetTaskUseCaseImpl implements GetTaskUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public GetTaskUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            TaskMapper taskMapper) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<TaskResult> getTasks(Long projectId, String status, String userEmail) {

        // Rule 1: Project phải tồn tại
        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Dự án không tồn tại"));

        // Rule 2: User phải tồn tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Rule 3: User phải là thành viên ACCEPTED
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của dự án này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa chấp nhận lời mời vào dự án này");
        }

        // Lấy danh sách Tasks
        List<Task> tasks;
        if (status == null || status.isBlank()) {
            // Không filter → lấy tất cả, sort theo position
            tasks = taskRepository.findAllByProjectIdOrderByPosition(projectId);
        } else {
            // Validate status hợp lệ
            TaskStatus taskStatus;
            try {
                taskStatus = TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Status không hợp lệ: '" + status + "'. Các giá trị hợp lệ: "
                        + Arrays.toString(TaskStatus.values())
                );
            }
            tasks = taskRepository.findAllByProjectIdAndStatusOrderByPositionAsc(projectId, taskStatus);
        }

        // Map → DTO (qua mapper)
        return tasks.stream()
                .map(taskMapper::toTaskResult)
                .toList();
    }
}
