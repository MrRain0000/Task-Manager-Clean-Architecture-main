package com.example.task_management.application.repositories.task;

import com.example.task_management.domain.entities.Task;
import java.util.List;

/**
 * Repository cho các thao tác ghi (command) Task
 */
public interface TaskCommandRepository {
    
    Task save(Task task);
    
    void saveAll(List<Task> tasks);
    
    void deleteById(Long id);
    
    void deleteAllByProjectId(Long projectId);
}
