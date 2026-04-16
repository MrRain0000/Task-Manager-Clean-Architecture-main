package com.example.task_management.application.usecases.impl.user;

import com.example.task_management.application.DTOUsecase.request.user.UpdateUserProfileCommand;
import com.example.task_management.application.DTOUsecase.response.user.UserProfileResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.user.UpdateUserProfileUseCase;
import com.example.task_management.domain.entities.User;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC22 – Cập nhật thông tin profile user.
 * Use Case đóng vai trò Orchestrator, chỉ cho phép cập nhật username.
 */
@Service
public class UpdateUserProfileUseCaseImpl implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public UpdateUserProfileUseCaseImpl(
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public UserProfileResult updateUserProfile(String userEmail, UpdateUserProfileCommand command) {
        // 1. Lấy user hiện tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 2. Cập nhật username nếu có
        if (command.getUsername() != null && !command.getUsername().isBlank()) {
            user.setUsername(command.getUsername().trim());
        }

        // 3. Lưu user
        User updatedUser = userRepository.save(user);

        // 4. Tính thống kê
        int totalProjects = projectMemberRepository.countByUserId(updatedUser.getId());
        int totalTasks = taskRepository.countByAssigneeId(updatedUser.getId());

        // 5. Return kết quả
        return UserProfileResult.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .isVerified(updatedUser.isVerified())
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .build();
    }
}
