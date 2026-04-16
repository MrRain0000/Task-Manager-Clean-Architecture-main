package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.ProjectMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberJpaRepository extends JpaRepository<ProjectMemberJpaEntity, Long> {
    Optional<ProjectMemberJpaEntity> findByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMemberJpaEntity> findAllByProjectId(Long projectId);
    List<ProjectMemberJpaEntity> findAllByUserIdAndInvitationStatus(Long userId, InvitationStatus status);
    int countByUserId(Long userId);
    void deleteAllByProjectId(Long projectId);
}
