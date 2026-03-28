package com.example.task_management.domain.services.impl;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.impl.task.AssignTaskUseCaseImpl;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.services.TaskAssignerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskAssignerServiceImpl implements TaskAssignerService {

    private static final Logger log = LoggerFactory.getLogger(AssignTaskUseCaseImpl.class);

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public TaskAssignerServiceImpl(ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void validateAssignerMembership(Long projectId, Long assignerId) {
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, assignerId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên của dự án này"));

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Bạn chưa chấp nhận lời mời vào dự án này");
        }
    }

    @Override
    public void validateAssignee(Long projectId, Long assigneeId) {
        // Check user tồn tại
        userRepository.findById(assigneeId)
                .orElseThrow(() -> {
                    log.error("[AssignTask] Assignee không tồn tại: assigneeId={}", assigneeId);
                    return new IllegalArgumentException("Người dùng được giao không tồn tại");
                });

        // Check assignee là thành viên ACCEPTED của project
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, assigneeId)
                .orElseThrow(() -> {
                    log.error("[AssignTask] Assignee không phải thành viên project: assigneeId={}, projectId={}", 
                            assigneeId, projectId);
                    return new IllegalArgumentException("Người được giao phải là thành viên của dự án");
                });

        if (membership.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            log.error("[AssignTask] Assignee chưa ACCEPTED: assigneeId={}, status={}", 
                    assigneeId, membership.getInvitationStatus());
            throw new IllegalArgumentException("Người được giao phải là thành viên chính thức của dự án");
        }
        log.debug("[AssignTask] Assignee validation OK");
    }
}
