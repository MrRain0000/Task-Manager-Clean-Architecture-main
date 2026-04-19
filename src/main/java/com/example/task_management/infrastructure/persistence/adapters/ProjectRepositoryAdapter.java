package com.example.task_management.infrastructure.persistence.adapters;

import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.infrastructure.persistence.jpaentities.ProjectJpaEntity;
import com.example.task_management.infrastructure.persistence.jparepositories.ProjectJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository projectJpaRepository;

    public ProjectRepositoryAdapter(ProjectJpaRepository projectJpaRepository) {
        this.projectJpaRepository = projectJpaRepository;
    }

    @Override
    public Project save(Project project) {
        ProjectJpaEntity entity = new ProjectJpaEntity();
        if (project.getId() != null) {
            entity.setId(project.getId());
        }
        entity.setName(project.getName());
        entity.setDescription(project.getDescription());
        entity.setOwnerId(project.getOwnerId());
        entity.setDeadline(project.getDeadline());
        
        ProjectJpaEntity savedEntity = projectJpaRepository.save(entity);
        
        Project savedProject = new Project();
        savedProject.setId(savedEntity.getId());
        savedProject.setName(savedEntity.getName());
        savedProject.setDescription(savedEntity.getDescription());
        savedProject.setOwnerId(savedEntity.getOwnerId());
        savedProject.setDeadline(savedEntity.getDeadline());
        
        return savedProject;
    }

    @Override
    public Optional<Project> findById(Long id) {
        return projectJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Project> findAllByOwnerId(Long ownerId) {
        return projectJpaRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        projectJpaRepository.deleteById(id);
    }

    private Project toDomain(ProjectJpaEntity entity) {
        Project project = new Project();
        project.setId(entity.getId());
        project.setName(entity.getName());
        project.setDescription(entity.getDescription());
        project.setOwnerId(entity.getOwnerId());
        project.setDeadline(entity.getDeadline());
        return project;
    }
}
