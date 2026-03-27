package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.dto.request.task.CreateTaskRequest;
import com.example.task_management.application.dto.response.task.TaskResponse;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.task.CreateTaskUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.factory.TaskFactory;
import com.example.task_management.infrastructure.persistence.adapters.TaskRepositoryAdapter;
import com.example.task_management.interfaces.mappers.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

        private final ProjectRepository projectRepository;
        private final ProjectMemberRepository projectMemberRepository;
        private final UserRepository userRepository;
        private final TaskRepositoryAdapter taskRepositoryAdapter;
        private final TaskMapper taskMapper;

        public CreateTaskUseCaseImpl(
                        ProjectRepository projectRepository,
                        ProjectMemberRepository projectMemberRepository,
                        UserRepository userRepository,
                        TaskRepositoryAdapter taskRepositoryAdapter,
                        TaskMapper taskMapper) {
                this.projectRepository = projectRepository;
                this.projectMemberRepository = projectMemberRepository;
                this.userRepository = userRepository;
                this.taskRepositoryAdapter = taskRepositoryAdapter;
                this.taskMapper = taskMapper;
        }

        @Override
        @Transactional
        public TaskResponse createTask(Long projectId, CreateTaskRequest request, String userEmail) {

                // Rule 1: Project phải tồn tại
                projectRepository.findById(projectId)
                                .orElseThrow(() -> new IllegalArgumentException("Dự án không tồn tại"));

                // Rule 2: User phải tồn tại
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

                // Rule 3: User phải thuộc dự án (là MEMBER hoặc OWNER)
                ProjectMember membership = projectMemberRepository
                                .findByProjectIdAndUserId(projectId, user.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Bạn không phải thành viên của dự án này"));

                // Rule 4: User phải ở trạng thái ACCEPTED (không được PENDING/REJECTED)
                if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
                        throw new IllegalArgumentException(
                                        "Bạn chưa chấp nhận lời mời tham gia dự án. Trạng thái hiện tại: "
                                                        + membership.getInvitationStatus());
                }

                // Rule 5: Tính position = Task cuối + 1 (Factory xử lý)
                int currentCount = taskRepositoryAdapter.countByProjectId(projectId);

                // Rule 6: Tạo Task qua Factory (status=TODO, assigneeId=null, position tự động)
                Task task = TaskFactory.create(request.getTitle(), request.getDescription(), projectId, currentCount);

                // Lưu vào Database
                Task savedTask = taskRepositoryAdapter.save(task);

                // Trả về DTO sử dụng mapper
                return taskMapper.toTaskResponse(savedTask);
        }
}
