package com.example.task_management.application.usecases.impl.user;

import com.example.task_management.application.DTOUsecase.response.user.UserProfileResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.user.GetUserProfileUseCase;
import com.example.task_management.domain.entities.User;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC21 – Lấy thông tin profile user hiện tại.
 * Use Case đóng vai trò Orchestrator, tổng hợp thông tin user và thống kê.
 */
@Service
public class GetUserProfileUseCaseImpl implements GetUserProfileUseCase {

    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public GetUserProfileUseCaseImpl(
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResult getUserProfile(String userEmail) {
        // 1. Lấy user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 2. Tính thống kê
        int totalProjects = projectMemberRepository.countByUserId(user.getId());
        int totalTasks = taskRepository.countByAssigneeId(user.getId());

        // 3. Return kết quả
        return UserProfileResult.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isVerified(user.isVerified())
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .build();
    }
}
