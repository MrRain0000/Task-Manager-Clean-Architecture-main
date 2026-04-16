package com.example.task_management.application.repositories;

import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository {
    ProjectMember save(ProjectMember projectMember);
    Optional<ProjectMember> findById(Long id);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMember> findAllByProjectId(Long projectId);
    List<ProjectMember> findAllByUserIdAndInvitationStatus(Long userId, InvitationStatus status);
    int countByUserId(Long userId);
    void deleteAllByProjectId(Long projectId);
}
