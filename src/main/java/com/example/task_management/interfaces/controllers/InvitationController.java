package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.response.project.InvitationResult;
import com.example.task_management.interfaces.dto.request.project.InviteMemberRequest;
import com.example.task_management.interfaces.dto.request.project.RespondInvitationRequest;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.project.InvitationResponse;
import com.example.task_management.application.usecases.project.GetPendingInvitationsUseCase;
import com.example.task_management.application.usecases.project.InviteMemberUseCase;
import com.example.task_management.application.usecases.project.RespondInvitationUseCase;
import com.example.task_management.interfaces.mappers.InvitationResponseMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvitationController {

    private final InviteMemberUseCase inviteMemberUseCase;
    private final GetPendingInvitationsUseCase getPendingInvitationsUseCase;
    private final RespondInvitationUseCase respondInvitationUseCase;
    private final InvitationResponseMapper invitationResponseMapper;

    public InvitationController(
            InviteMemberUseCase inviteMemberUseCase, 
            GetPendingInvitationsUseCase getPendingInvitationsUseCase, 
            RespondInvitationUseCase respondInvitationUseCase,
            InvitationResponseMapper invitationResponseMapper) {
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.getPendingInvitationsUseCase = getPendingInvitationsUseCase;
        this.respondInvitationUseCase = respondInvitationUseCase;
        this.invitationResponseMapper = invitationResponseMapper;
    }

    // 1. API - Gửi lời mời (Dành cho Chủ Dự án)
    @PostMapping("/projects/{projectId}/invite")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteMemberRequest request,
            Authentication authentication) {
        
        inviteMemberUseCase.inviteMember(projectId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Gửi lời mời thành công", null));
    }

    // 2. API - Lấy danh sách Lời mời chưa giải quyết (Dành cho người được mời)
    // Note: Trả về array trực tiếp, không bọc trong ApiResponse (theo API_DOCS.md)
    @GetMapping("/users/me/invitations")
    public ResponseEntity<List<InvitationResponse>> getMyPendingInvitations(Authentication authentication) {

        List<InvitationResult> results = getPendingInvitationsUseCase.getPendingInvitations(authentication.getName());
        List<InvitationResponse> pendingInvitations = results.stream()
                .map(invitationResponseMapper::toInvitationResponse)
                .toList();
        return ResponseEntity.ok(pendingInvitations);
    }

    // 3. API - Quyết định Chấp nhận/Từ chối Lời mời (Dành cho người được mời)
    @PostMapping("/projects/{projectId}/invitations/respond")
    public ResponseEntity<ApiResponse<Void>> respondInvitation(
            @PathVariable Long projectId,
            @Valid @RequestBody RespondInvitationRequest request,
            Authentication authentication) {
        
        respondInvitationUseCase.respondInvitation(projectId, request, authentication.getName());
        String action = Boolean.TRUE.equals(request.getIsAccept()) ? "Chấp nhận" : "Từ chối";
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Đã " + action + " lời mời vào dự án", null));
    }
}
