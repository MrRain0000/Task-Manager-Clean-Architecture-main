package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.DTOUsecase.request.project.UpdateProjectCommand;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.application.mapper.ProjectMapper;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.User;
import com.example.task_management.interfaces.exceptions.ProjectAccessDeniedException;
import com.example.task_management.interfaces.exceptions.ProjectNotFoundException;
import com.example.task_management.interfaces.exceptions.ProjectValidationException;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import com.example.task_management.domain.services.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProjectUseCaseImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private LogActivityUseCase logActivityUseCase;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private UpdateProjectUseCaseImpl updateProjectUseCase;

    private final String currentUserEmail = "owner@example.com";
    private User owner;
    private Project project;
    private UpdateProjectCommand request;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail(currentUserEmail);

        project = new Project();
        project.setId(10L);
        project.setName("Old Name");
        project.setDescription("Old Description");
        project.setOwnerId(1L);

        request = new UpdateProjectCommand("  New Name  ", "  New Description  ");
        when(permissionService.canUpdateProject(any(Project.class), any(User.class))).thenReturn(true);
    }

    @Test
    void updateProject_Success_ShouldUpdateAndReturnResult() {
        ProjectResult response = ProjectResult.builder()
                .id(10L)
                .name("New Name")
                .description("New Description")
                .ownerId(1L)
                .build();

        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.toProjectResponse(any(Project.class))).thenReturn(response);

        ProjectResult result = updateProjectUseCase.updateProject(10L, request, currentUserEmail);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(logActivityUseCase, times(1)).logActivity(any());
    }

    @Test
    void updateProject_UserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> updateProjectUseCase.updateProject(10L, request, currentUserEmail));
        verify(projectRepository, never()).findById(any());
    }

    @Test
    void updateProject_ProjectNotFound_ShouldThrowException() {
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> updateProjectUseCase.updateProject(10L, request, currentUserEmail));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_NotOwner_ShouldThrowException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("member@example.com");

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(anotherUser));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(permissionService.canUpdateProject(project, anotherUser)).thenReturn(false);

        assertThrows(ProjectAccessDeniedException.class,
                () -> updateProjectUseCase.updateProject(10L, request, "member@example.com"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_BlankName_ShouldThrowValidationException() {
        UpdateProjectCommand invalidRequest = new UpdateProjectCommand("   ", "Desc");
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThrows(ProjectValidationException.class,
                () -> updateProjectUseCase.updateProject(10L, invalidRequest, currentUserEmail));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_DescriptionTooLong_ShouldThrowValidationException() {
        String longDescription = "a".repeat(501);
        UpdateProjectCommand invalidRequest = new UpdateProjectCommand("Name", longDescription);
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(permissionService.canUpdateProject(project, owner)).thenReturn(true);

        assertThrows(ProjectValidationException.class,
                () -> updateProjectUseCase.updateProject(10L, invalidRequest, currentUserEmail));
        verify(projectRepository, never()).save(any());
    }
}
