package com.example.task_management.infrastructure.persistence.adapters;

import com.example.task_management.application.repositories.subtask.SubTaskCommandRepository;
import com.example.task_management.application.repositories.subtask.SubTaskQueryRepository;
import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.SubTaskJpaEntity;
import com.example.task_management.infrastructure.persistence.jparepositories.SubTaskJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter chuyển đổi giữa domain SubTask và JPA SubTaskJpaEntity
 */
@Component
public class SubTaskRepositoryAdapter implements SubTaskCommandRepository, SubTaskQueryRepository {

    private final SubTaskJpaRepository subTaskJpaRepository;

    public SubTaskRepositoryAdapter(SubTaskJpaRepository subTaskJpaRepository) {
        this.subTaskJpaRepository = subTaskJpaRepository;
    }

    // ============== Command Operations ==============

    @Override
    public SubTask save(SubTask subTask) {
        SubTaskJpaEntity entity = toJpaEntity(subTask);
        SubTaskJpaEntity saved = subTaskJpaRepository.save(entity);
        return toDomainEntity(saved);
    }

    @Override
    public void deleteById(Long id) {
        subTaskJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByTaskId(Long taskId) {
        subTaskJpaRepository.deleteAllByTaskId(taskId);
    }

    @Override
    public void reorderSubTasks(Long taskId, List<Long> subtaskIds) {
        for (int i = 0; i < subtaskIds.size(); i++) {
            final int position = i;
            Long subtaskId = subtaskIds.get(i);
            subTaskJpaRepository.findById(subtaskId).ifPresent(entity -> {
                entity.setPosition(position);
                subTaskJpaRepository.save(entity);
            });
        }
    }

    // ============== Query Operations ==============

    @Override
    public Optional<SubTask> findById(Long id) {
        return subTaskJpaRepository.findById(id)
                .map(this::toDomainEntity);
    }

    @Override
    public List<SubTask> findAllByTaskId(Long taskId) {
        return subTaskJpaRepository.findAllByTaskId(taskId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubTask> findAllByTaskIdOrderByPositionAsc(Long taskId) {
        return subTaskJpaRepository.findAllByTaskIdOrderByPositionAsc(taskId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubTask> findAllByTaskIdAndStatus(Long taskId, TaskStatus status) {
        return subTaskJpaRepository.findAllByTaskIdAndStatus(taskId, status).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int countByTaskId(Long taskId) {
        return subTaskJpaRepository.countByTaskId(taskId);
    }

    @Override
    public int countByTaskIdAndStatus(Long taskId, TaskStatus status) {
        return subTaskJpaRepository.countByTaskIdAndStatus(taskId, status);
    }

    @Override
    public Integer findMaxPositionByTaskId(Long taskId) {
        return subTaskJpaRepository.findMaxPositionByTaskId(taskId);
    }

    // ============== Mapping Methods ==============

    private SubTaskJpaEntity toJpaEntity(SubTask domain) {
        SubTaskJpaEntity entity = new SubTaskJpaEntity();
        entity.setId(domain.getId());
        entity.setTaskId(domain.getTaskId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setAssigneeId(domain.getAssigneeId());
        entity.setPriority(domain.getPriority());
        entity.setStatus(domain.getStatus());
        entity.setPosition(domain.getPosition());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private SubTask toDomainEntity(SubTaskJpaEntity entity) {
        SubTask domain = new SubTask();
        domain.setId(entity.getId());
        domain.setTaskId(entity.getTaskId());
        domain.setTitle(entity.getTitle());
        domain.setDescription(entity.getDescription());
        domain.setAssigneeId(entity.getAssigneeId());
        domain.setPriority(entity.getPriority());
        domain.setStatus(entity.getStatus());
        domain.setPosition(entity.getPosition());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
