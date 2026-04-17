package com.example.task_management.application.repositories.subtask;

import com.example.task_management.domain.entities.SubTask;
import com.example.task_management.domain.enums.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho read operations của SubTask
 */
public interface SubTaskQueryRepository {

    Optional<SubTask> findById(Long id);

    List<SubTask> findAllByTaskId(Long taskId);

    List<SubTask> findAllByTaskIdOrderByPositionAsc(Long taskId);

    List<SubTask> findAllByTaskIdAndStatus(Long taskId, TaskStatus status);

    int countByTaskId(Long taskId);

    int countByTaskIdAndStatus(Long taskId, TaskStatus status);

    Integer findMaxPositionByTaskId(Long taskId);
}
