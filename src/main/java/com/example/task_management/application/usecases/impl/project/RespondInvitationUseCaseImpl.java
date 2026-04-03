package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.interfaces.dto.request.project.RespondInvitationRequest;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.project.RespondInvitationUseCase;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import java.util.Map;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RespondInvitationUseCaseImpl implements RespondInvitationUseCase {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final LogActivityUseCase logActivityUseCase;

    public RespondInvitationUseCaseImpl(ProjectMemberRepository projectMemberRepository, UserRepository userRepository, LogActivityUseCase logActivityUseCase) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.logActivityUseCase = logActivityUseCase;
    }

    @Override
    @Transactional
    public void respondInvitation(Long projectId, RespondInvitationRequest request, String userEmail) {
        // 1. Xác minh thân phận người bị mời
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 2. Tra cứu Lời mời thông qua projectId & userId
        ProjectMember invitation = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời cho bạn tại dự án này"));

        // 3. Validation
        if (invitation.getInvitationStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời này không ở trạng thái Chờ xác nhận (PENDING). Status hiện tại: " + invitation.getInvitationStatus());
        }

        // 4. Update Status dựa theo Payload gửi lên
        if (Boolean.TRUE.equals(request.getIsAccept())) {
            invitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            projectMemberRepository.save(invitation);

            // Ghi log khi user chấp nhận lời mời
            logActivityUseCase.logActivity(LogActivityRequest.builder()
                    .projectId(projectId)
                    .userId(user.getId())
                    .actionType(ActionType.MEMBER_JOINED)
                    .entityType(EntityType.MEMBER)
                    .entityId(user.getId())
                    .description(user.getUsername() + " joined the project")
                    .metadata(Map.of(
                            "userId", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail()
                    ))
                    .build());
        } else {
            invitation.setInvitationStatus(InvitationStatus.REJECTED);
            projectMemberRepository.save(invitation);
            // (Tuỳ chọn: Bạn có thể Xóa hẳn record này ra khỏi bảng nếu không có nhu cầu lưu Log rác 
            // hiện tại mình ưu tiên Giữ Log lại và update chữ REJECTED để truy vết).
        }
    }
}
