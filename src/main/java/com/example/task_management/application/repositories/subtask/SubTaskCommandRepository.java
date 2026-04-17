package com.example.task_management.application.repositories.subtask;

import com.example.task_management.domain.entities.SubTask;

import java.util.List;

/**
 * Repository interface cho write operations của SubTask
 */
public interface SubTaskCommandRepository {

    SubTask save(SubTask subTask);

    void deleteById(Long id);

    void deleteAllByTaskId(Long taskId);

    void reorderSubTasks(Long taskId, List<Long> subtaskIds);
}
