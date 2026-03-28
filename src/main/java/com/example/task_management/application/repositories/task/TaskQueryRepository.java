package com.example.task_management.application.repositories.task;

import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.enums.TaskStatus;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho các thao tác đọc (query) Task
 */
public interface TaskQueryRepository {
    
    Optional<Task> findById(Long id);
    
    List<Task> findAllByProjectId(Long projectId);
    
    List<Task> findAllByProjectIdOrderByPosition(Long projectId);
    
    List<Task> findAllByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status);
    
    List<Task> findAllByProjectIdAndStatusAndPositionGreaterThanEqual(Long projectId, TaskStatus status, Integer position);

    int countByProjectId(Long projectId);
}
