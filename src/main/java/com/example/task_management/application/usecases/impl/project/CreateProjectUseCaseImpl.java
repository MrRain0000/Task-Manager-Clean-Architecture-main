package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.interfaces.dto.request.project.CreateProjectRequest;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.project.CreateProjectUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.factory.ProjectFactory;
import com.example.task_management.domain.factory.ProjectMemberFactory;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import java.util.Map;
import com.example.task_management.application.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjectUseCaseImpl implements CreateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final LogActivityUseCase logActivityUseCase;

    public CreateProjectUseCaseImpl(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            ProjectMapper projectMapper,
            LogActivityUseCase logActivityUseCase) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
        this.logActivityUseCase = logActivityUseCase;
    }

    @Override
    @Transactional
    public ProjectResult createProject(CreateProjectRequest request, String currentUserEmail) {
        // 1. Lấy thông tin User hiện tại từ Database thông qua Email lấy từ
        // SecurityContext
        User owner = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng hiện tại trong hệ thống."));

        // 2. Khởi tạo đối tượng Project theo đúng Request
        Project project = ProjectFactory.create(request.getName(), request.getDescription(), owner.getId(), request.getDueDate());

        Project savedProject = projectRepository.save(project);

        // 3. Tự động thêm User tạo dự án thành Thành viên có quyền cao nhất (OWNER) của
        // dự á
        ProjectMember member = ProjectMemberFactory.createOwner(savedProject.getId(), owner.getId());

        projectMemberRepository.save(member);

        // Ghi log hoạt động (async)
        logActivityUseCase.logActivity(LogActivityRequest.builder()
                .projectId(savedProject.getId())
                .userId(owner.getId())
                .actionType(ActionType.PROJECT_CREATED)
                .entityType(EntityType.PROJECT)
                .entityId(savedProject.getId())
                .description("Created project: " + savedProject.getName())
                .metadata(Map.of("projectName", savedProject.getName()))
                .build());

        // 4. Trả về Response DTO ra ngoài thông qua Mapper
        return projectMapper.toProjectResponse(savedProject);
    }
}
