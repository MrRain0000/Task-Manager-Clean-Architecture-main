package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.TaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long> {
    List<TaskJpaEntity> findAllByProjectId(Long projectId);
    List<TaskJpaEntity> findAllByProjectIdOrderByPositionAsc(Long projectId);
    List<TaskJpaEntity> findAllByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status);
    List<TaskJpaEntity> findAllByProjectIdAndStatusAndPositionGreaterThanEqualOrderByPositionAsc(Long projectId, TaskStatus status, Integer position);
    void deleteAllByProjectId(Long projectId);
    int countByProjectId(Long projectId);
}
