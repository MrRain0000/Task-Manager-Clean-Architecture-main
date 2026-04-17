package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.SubTaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubTaskJpaRepository extends JpaRepository<SubTaskJpaEntity, Long> {

    Optional<SubTaskJpaEntity> findById(Long id);

    List<SubTaskJpaEntity> findAllByTaskId(Long taskId);

    List<SubTaskJpaEntity> findAllByTaskIdOrderByPositionAsc(Long taskId);

    List<SubTaskJpaEntity> findAllByTaskIdAndStatus(Long taskId, TaskStatus status);

    int countByTaskId(Long taskId);

    int countByTaskIdAndStatus(Long taskId, TaskStatus status);

    @Query("SELECT COALESCE(MAX(s.position), -1) FROM SubTaskJpaEntity s WHERE s.taskId = :taskId")
    Integer findMaxPositionByTaskId(@Param("taskId") Long taskId);

    void deleteAllByTaskId(Long taskId);
}
