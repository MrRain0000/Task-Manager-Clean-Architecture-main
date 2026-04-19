package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.DTOUsecase.request.project.UpdateProjectCommand;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.application.mapper.ProjectMapper;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.events.ProjectUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.example.task_management.application.usecases.project.UpdateProjectUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.User;
import com.example.task_management.interfaces.exceptions.ProjectAccessDeniedException;
import com.example.task_management.interfaces.exceptions.ProjectNotFoundException;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import com.example.task_management.domain.services.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProjectUseCaseImpl implements UpdateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PermissionService permissionService;

    public UpdateProjectUseCaseImpl(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMapper projectMapper,
            ApplicationEventPublisher eventPublisher,
            PermissionService permissionService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
        this.eventPublisher = eventPublisher;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public ProjectResult updateProject(Long projectId, UpdateProjectCommand request, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng hiện tại trong hệ thống."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Dự án không tồn tại."));

        if (!permissionService.canUpdateProject(project, currentUser)) {
            throw new ProjectAccessDeniedException("Bạn không có quyền sửa dự án này do không phải là Owner.");
        }

        String normalizedName = project.normalizeName(request.getName());
        
        String normalizedDescription = project.normalizeDescription(request.getDescription());

        String oldName = project.getName();
        String oldDescription = project.getDescription();

        project.setName(normalizedName);
        project.setDescription(normalizedDescription);
        
        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }

        Project updatedProject = projectRepository.save(project);

        eventPublisher.publishEvent(new ProjectUpdatedEvent(
                updatedProject.getId(),
                currentUser.getId(),
                updatedProject.getName(),
                updatedProject.getDescription(),
                oldName,
                oldDescription
        ));

        return projectMapper.toProjectResponse(updatedProject);
    }

}
