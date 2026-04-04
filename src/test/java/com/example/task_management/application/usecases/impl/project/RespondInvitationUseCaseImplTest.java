package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.interfaces.dto.request.project.RespondInvitationRequest;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
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
class RespondInvitationUseCaseImplTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogActivityUseCase logActivityUseCase;

    @InjectMocks
    private RespondInvitationUseCaseImpl respondInvitationUseCase;

    private User invitee;
    private ProjectMember invitation;
    private RespondInvitationRequest request;
    
    private final String inviteeEmail = "member@test.com";
    private final Long projectId = 1L;

    @BeforeEach
    void setUp() {
        invitee = new User();
        invitee.setId(20L);
        invitee.setEmail(inviteeEmail);
        invitee.setUsername("member");

        invitation = new ProjectMember();
        invitation.setProjectId(projectId);
        invitation.setUserId(invitee.getId());
        invitation.setInvitationStatus(InvitationStatus.PENDING);

        request = new RespondInvitationRequest();
    }

    @Test
    void respondInvitation_Accept_ShouldUpdateStatusToAccepted() {
        // Arrange
        request.setIsAccept(true);
        when(userRepository.findByEmail(inviteeEmail)).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, invitee.getId())).thenReturn(Optional.of(invitation));

        // Act
        respondInvitationUseCase.respondInvitation(projectId, request, inviteeEmail);

        // Assert
        assertEquals(InvitationStatus.ACCEPTED, invitation.getInvitationStatus());
        verify(projectMemberRepository, times(1)).save(invitation);
    }

    @Test
    void respondInvitation_Reject_ShouldUpdateStatusToRejected() {
        // Arrange
        request.setIsAccept(false);
        when(userRepository.findByEmail(inviteeEmail)).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, invitee.getId())).thenReturn(Optional.of(invitation));

        // Act
        respondInvitationUseCase.respondInvitation(projectId, request, inviteeEmail);

        // Assert
        assertEquals(InvitationStatus.REJECTED, invitation.getInvitationStatus());
        verify(projectMemberRepository, times(1)).save(invitation);
    }

    @Test
    void respondInvitation_NotPending_ShouldThrowException() {
        // Arrange
        invitation.setInvitationStatus(InvitationStatus.ACCEPTED); // Changed from PENDING
        request.setIsAccept(true);
        when(userRepository.findByEmail(inviteeEmail)).thenReturn(Optional.of(invitee));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, invitee.getId())).thenReturn(Optional.of(invitation));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            respondInvitationUseCase.respondInvitation(projectId, request, inviteeEmail);
        });

        assertEquals("Lời mời này không ở trạng thái Chờ xác nhận (PENDING). Status hiện tại: ACCEPTED", exception.getMessage());
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }
}
