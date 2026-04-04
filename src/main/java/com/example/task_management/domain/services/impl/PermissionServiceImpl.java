package com.example.task_management.domain.services.impl;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.impl.task.MoveTaskUseCaseImpl;

import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.services.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private static final Logger log = LoggerFactory.getLogger(MoveTaskUseCaseImpl.class);

    public PermissionServiceImpl(UserRepository userRepository, ProjectMemberRepository projectMemberRepository) {
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public Long validateProjectMember(Long projectId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("[MoveTask] User không tồn tại");
                    return new IllegalArgumentException("Người dùng không tồn tại");
                });
        log.debug("[MoveTask] User tồn tại: id={}", user.getId());

        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> {
                    log.error("[MoveTask] User không phải thành viên project: userId={}", user.getId());
                    return new IllegalArgumentException("Bạn không phải thành viên của dự án này");
                });

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            log.error("[MoveTask] User chưa ACCEPTED: userId={}, status={}",
                    user.getId(), membership.getInvitationStatus());
            throw new IllegalArgumentException("Bạn chưa chấp nhận lời mời vào dự án này");
        }
        log.debug("[MoveTask] User permission OK");
        return user.getId();
    }

    @Override
    public boolean canUpdateProject(Project project, User user) {
        return project.getOwnerId().equals(user.getId());
    }
}
