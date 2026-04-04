package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.interfaces.dto.request.project.InviteMemberRequest;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.MemberRole;
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
class InviteMemberUseCaseImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogActivityUseCase logActivityUseCase;

    @InjectMocks
    private InviteMemberUseCaseImpl inviteMemberUseCase;

    private User inviter;
    private User invitee;
    private Project project;
    private ProjectMember inviterMembership;
    private InviteMemberRequest request;
    
    private final String inviterEmail = "owner@test.com";
    private final String inviteeEmail = "member@test.com";
    private final Long projectId = 1L;

    @BeforeEach
    void setUp() {
        inviter = new User();
        inviter.setId(10L);
        inviter.setEmail(inviterEmail);

        invitee = new User();
        invitee.setId(20L);
        invitee.setEmail(inviteeEmail);
        invitee.setVerified(true);

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");

        inviterMembership = new ProjectMember();
        inviterMembership.setProjectId(projectId);
        inviterMembership.setUserId(inviter.getId());
        inviterMembership.setRole(MemberRole.OWNER);
        inviterMembership.setInvitationStatus(InvitationStatus.ACCEPTED);

        request = new InviteMemberRequest();
        request.setInviteeEmail(inviteeEmail);
    }

    @Test
    void inviteMember_Success_ShouldSaveProjectMember() {
        // Arrange
        when(userRepository.findByEmail(inviterEmail)).thenReturn(Optional.of(inviter));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, inviter.getId())).thenReturn(Optional.of(inviterMembership));
        when(userRepository.findByEmail(inviteeEmail)).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, invitee.getId())).thenReturn(Optional.empty());

        // Act
        inviteMemberUseCase.inviteMember(projectId, request, inviterEmail);

        // Assert
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository, times(1)).save(memberCaptor.capture());

        ProjectMember savedMember = memberCaptor.getValue();
        assertEquals(projectId, savedMember.getProjectId());
        assertEquals(invitee.getId(), savedMember.getUserId());
        assertEquals(MemberRole.MEMBER, savedMember.getRole());
        assertEquals(InvitationStatus.PENDING, savedMember.getInvitationStatus());
    }

    @Test
    void inviteMember_InviterNotOwner_ShouldThrowException() {
        // Arrange
        inviterMembership.setRole(MemberRole.MEMBER); // Not an owner
        when(userRepository.findByEmail(inviterEmail)).thenReturn(Optional.of(inviter));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, inviter.getId())).thenReturn(Optional.of(inviterMembership));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inviteMemberUseCase.inviteMember(projectId, request, inviterEmail);
        });

        assertEquals("Chỉ OWNER mới được mời", exception.getMessage());
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void inviteMember_SelfInvite_ShouldThrowException() {
        // Arrange
        request.setInviteeEmail(inviterEmail);
        when(userRepository.findByEmail(inviterEmail)).thenReturn(Optional.of(inviter));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, inviter.getId())).thenReturn(Optional.of(inviterMembership));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inviteMemberUseCase.inviteMember(projectId, request, inviterEmail);
        });

        assertEquals("Không thể tự mời chính mình", exception.getMessage());
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void inviteMember_InviteeAlreadyInProject_ShouldThrowException() {
        // Arrange
        ProjectMember existingMember = new ProjectMember();
        when(userRepository.findByEmail(inviterEmail)).thenReturn(Optional.of(inviter));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, inviter.getId())).thenReturn(Optional.of(inviterMembership));
        when(userRepository.findByEmail(inviteeEmail)).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, invitee.getId())).thenReturn(Optional.of(existingMember));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inviteMemberUseCase.inviteMember(projectId, request, inviterEmail);
        });

        assertEquals("User đã ở trong project hoặc đã được mời", exception.getMessage());
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }
}
