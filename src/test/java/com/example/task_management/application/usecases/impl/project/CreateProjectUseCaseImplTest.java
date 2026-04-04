package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.interfaces.dto.request.project.CreateProjectRequest;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.MemberRole;
import com.example.task_management.application.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProjectUseCaseImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private LogActivityUseCase logActivityUseCase;

    @InjectMocks
    private CreateProjectUseCaseImpl createProjectUseCase;

    private User owner;
    private CreateProjectRequest request;
    private Project savedProject;
    private ProjectResult response;
    private String currentUserEmail = "owner@example.com";

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail(currentUserEmail);

        request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setDescription("Test Description");

        savedProject = new Project();
        savedProject.setId(10L);
        savedProject.setName("Test Project");
        savedProject.setDescription("Test Description");
        savedProject.setOwnerId(owner.getId());

        response = ProjectResult.builder()
                .id(10L)
                .name("Test Project")
                .build();
    }

    @Test
    void createProject_Success_ShouldSaveProjectAndOwner() {
        // Arrange
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMapper.toProjectResponse(savedProject)).thenReturn(response);

        // Act
        ProjectResult result = createProjectUseCase.createProject(request, currentUserEmail);

        // Assert
        assertNotNull(result);
        assertEquals(response.getId(), result.getId());
        
        verify(userRepository, times(1)).findByEmail(currentUserEmail);
        verify(projectRepository, times(1)).save(any(Project.class));
        
        // Verify owner is added as project member
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository, times(1)).save(memberCaptor.capture());
        
        ProjectMember savedMember = memberCaptor.getValue();
        assertEquals(savedProject.getId(), savedMember.getProjectId());
        assertEquals(owner.getId(), savedMember.getUserId());
        assertEquals(MemberRole.OWNER, savedMember.getRole());
        assertEquals(InvitationStatus.ACCEPTED, savedMember.getInvitationStatus());
    }

    @Test
    void createProject_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createProjectUseCase.createProject(request, currentUserEmail);
        });
        
        assertEquals("Không tìm thấy người dùng hiện tại trong hệ thống.", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }
}
