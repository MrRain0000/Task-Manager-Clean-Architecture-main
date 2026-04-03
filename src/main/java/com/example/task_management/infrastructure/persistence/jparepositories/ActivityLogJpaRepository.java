package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.infrastructure.persistence.jpaentities.ActivityLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository cho ActivityLog.
 * Sử dụng Spring Data JPA với method name conventions.
 */
@Repository
public interface ActivityLogJpaRepository extends JpaRepository<ActivityLogJpaEntity, Long> {

    Page<ActivityLogJpaEntity> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    Page<ActivityLogJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ActivityLogJpaEntity> findByActionTypeOrderByCreatedAtDesc(ActionType actionType, Pageable pageable);

    List<ActivityLogJpaEntity> findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long projectId, LocalDateTime from, LocalDateTime to);

    List<ActivityLogJpaEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId);
}
