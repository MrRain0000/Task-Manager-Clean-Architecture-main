package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.TaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long> {
    List<TaskJpaEntity> findAllByProjectId(Long projectId);
    List<TaskJpaEntity> findAllByProjectIdOrderByPositionAsc(Long projectId);
    List<TaskJpaEntity> findAllByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status);
    List<TaskJpaEntity> findAllByProjectIdAndStatusAndPositionGreaterThanEqualOrderByPositionAsc(Long projectId, TaskStatus status, Integer position);
    void deleteAllByProjectId(Long projectId);
    int countByProjectId(Long projectId);
    int countByAssigneeId(Long assigneeId);
    
    // Search by keyword in title or description (case-insensitive)
    @Query("SELECT t FROM TaskJpaEntity t WHERE t.projectId = :projectId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TaskJpaEntity> searchByProjectIdAndKeyword(@Param("projectId") Long projectId, @Param("keyword") String keyword);
}
