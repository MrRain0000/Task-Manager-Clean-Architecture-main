package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProjectUseCaseImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogActivityUseCase logActivityUseCase;

    @InjectMocks
    private DeleteProjectUseCaseImpl deleteProjectUseCase;

    private User owner;
    private User notOwner;
    private Project project;
    private final String ownerEmail = "owner@test.com";
    private final String notOwnerEmail = "member@test.com";
    private final Long projectId = 1L;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(10L);
        owner.setEmail(ownerEmail);

        notOwner = new User();
        notOwner.setId(20L);
        notOwner.setEmail(notOwnerEmail);

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setOwnerId(owner.getId());
    }

    @Test
    void deleteProject_Success_ShouldDeleteTasksMembersAndProject() {
        // Arrange
        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        deleteProjectUseCase.deleteProject(projectId, ownerEmail);

        // Assert
        verify(taskRepository, times(1)).deleteAllByProjectId(projectId);
        verify(projectMemberRepository, times(1)).deleteAllByProjectId(projectId);
        verify(projectRepository, times(1)).deleteById(projectId);
    }

    @Test
    void deleteProject_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deleteProjectUseCase.deleteProject(projectId, ownerEmail);
        });

        assertEquals("Không tìm thấy người dùng hiện tại trong hệ thống.", exception.getMessage());
        verify(taskRepository, never()).deleteAllByProjectId(anyLong());
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProject_ProjectNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deleteProjectUseCase.deleteProject(projectId, ownerEmail);
        });

        assertEquals("Dự án không tồn tại.", exception.getMessage());
        verify(taskRepository, never()).deleteAllByProjectId(anyLong());
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProject_NotOwner_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(notOwnerEmail)).thenReturn(Optional.of(notOwner));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project)); // owner is 10L, but user is 20L

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deleteProjectUseCase.deleteProject(projectId, notOwnerEmail);
        });

        assertEquals("Bạn không có quyền xóa dự án này do không phải là Owner.", exception.getMessage());
        verify(taskRepository, never()).deleteAllByProjectId(anyLong());
        verify(projectRepository, never()).deleteById(anyLong());
    }
}
