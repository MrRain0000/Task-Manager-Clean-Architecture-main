package com.example.task_management.infrastructure.persistence.adapters;

import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.TaskJpaEntity;
import com.example.task_management.infrastructure.persistence.jparepositories.TaskJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class TaskRepositoryAdapter implements TaskRepository {

    private final TaskJpaRepository taskJpaRepository;

    public TaskRepositoryAdapter(TaskJpaRepository taskJpaRepository) {
        this.taskJpaRepository = taskJpaRepository;
    }

    @Override
    public Task save(Task task) {
        TaskJpaEntity entity = toJpaEntity(task);
        TaskJpaEntity saved = taskJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Task> findAllByProjectId(Long projectId) {
        return taskJpaRepository.findAllByProjectId(projectId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Task> findAllByProjectIdOrderByPosition(Long projectId) {
        return taskJpaRepository.findAllByProjectIdOrderByPositionAsc(projectId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Task> findAllByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status) {
        return taskJpaRepository.findAllByProjectIdAndStatusOrderByPositionAsc(projectId, status)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Task> findAllByProjectIdAndStatusAndPositionGreaterThanEqual(Long projectId, TaskStatus status, Integer position) {
        return taskJpaRepository.findAllByProjectIdAndStatusAndPositionGreaterThanEqualOrderByPositionAsc(projectId, status, position)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void saveAll(List<Task> tasks) {
        List<TaskJpaEntity> entities = tasks.stream().map(this::toJpaEntity).toList();
        taskJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteById(Long id) {
        taskJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByProjectId(Long projectId) {
        taskJpaRepository.deleteAllByProjectId(projectId);
    }

    public int countByProjectId(Long projectId) {
        return taskJpaRepository.countByProjectId(projectId);
    }

    @Override
    public int countByAssigneeId(Long assigneeId) {
        return taskJpaRepository.countByAssigneeId(assigneeId);
    }

    @Override
    public List<Task> searchByProjectIdAndKeyword(Long projectId, String keyword) {
        return taskJpaRepository.searchByProjectIdAndKeyword(projectId, keyword)
                .stream().map(this::toDomain).toList();
    }

    // ── Mappers ─────────────────────────────────────────────────────
    private TaskJpaEntity toJpaEntity(Task task) {
        TaskJpaEntity entity = new TaskJpaEntity();
        if (task.getId() != null) entity.setId(task.getId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatus(task.getStatus());
        entity.setProjectId(task.getProjectId());
        entity.setAssigneeId(task.getAssigneeId());
        entity.setPosition(task.getPosition());
        return entity;
    }

    private Task toDomain(TaskJpaEntity entity) {
        Task task = new Task();
        task.setId(entity.getId());
        task.setTitle(entity.getTitle());
        task.setDescription(entity.getDescription());
        task.setStatus(entity.getStatus());
        task.setProjectId(entity.getProjectId());
        task.setAssigneeId(entity.getAssigneeId());
        task.setPosition(entity.getPosition());
        return task;
    }
}
