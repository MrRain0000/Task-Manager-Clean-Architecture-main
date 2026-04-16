package com.example.task_management.infrastructure.persistence.adapters;

import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.ProjectMemberJpaEntity;
import com.example.task_management.infrastructure.persistence.jparepositories.ProjectMemberJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class ProjectMemberRepositoryAdapter implements ProjectMemberRepository {

    private final ProjectMemberJpaRepository projectMemberJpaRepository;

    public ProjectMemberRepositoryAdapter(ProjectMemberJpaRepository projectMemberJpaRepository) {
        this.projectMemberJpaRepository = projectMemberJpaRepository;
    }

    @Override
    public ProjectMember save(ProjectMember projectMember) {
        ProjectMemberJpaEntity entity = new ProjectMemberJpaEntity();
        if (projectMember.getId() != null) {
            entity.setId(projectMember.getId());
        }
        entity.setProjectId(projectMember.getProjectId());
        entity.setUserId(projectMember.getUserId());
        entity.setRole(projectMember.getRole());
        entity.setInvitationStatus(projectMember.getInvitationStatus());

        ProjectMemberJpaEntity savedEntity = projectMemberJpaRepository.save(entity);

        ProjectMember savedProjectMember = new ProjectMember();
        savedProjectMember.setId(savedEntity.getId());
        savedProjectMember.setProjectId(savedEntity.getProjectId());
        savedProjectMember.setUserId(savedEntity.getUserId());
        savedProjectMember.setRole(savedEntity.getRole());
        savedProjectMember.setInvitationStatus(savedEntity.getInvitationStatus());

        return savedProjectMember;
    }

    @Override
    public Optional<ProjectMember> findById(Long id) {
        return projectMemberJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMemberJpaRepository.findByProjectIdAndUserId(projectId, userId).map(this::toDomain);
    }

    @Override
    public List<ProjectMember> findAllByProjectId(Long projectId) {
        return projectMemberJpaRepository.findAllByProjectId(projectId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ProjectMember> findAllByUserIdAndInvitationStatus(Long userId, InvitationStatus status) {
        return projectMemberJpaRepository.findAllByUserIdAndInvitationStatus(userId, status)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ProjectMember toDomain(ProjectMemberJpaEntity entity) {
        ProjectMember member = new ProjectMember();
        member.setId(entity.getId());
        member.setProjectId(entity.getProjectId());
        member.setUserId(entity.getUserId());
        member.setRole(entity.getRole());
        member.setInvitationStatus(entity.getInvitationStatus());
        return member;
    }

    @Override
    public void deleteAllByProjectId(Long projectId) {
        projectMemberJpaRepository.deleteAllByProjectId(projectId);
    }

    @Override
    public int countByUserId(Long userId) {
        return projectMemberJpaRepository.countByUserId(userId);
    }
}
