package com.example.task_management.application.usecases.subtask;

import java.util.List;

/**
 * Use case sắp xếp lại thứ tự SubTasks
 */
public interface ReorderSubTasksUseCase {

    /**
     * Sắp xếp lại thứ tự các sub-tasks
     *
     * @param taskId      ID của task chính
     * @param subtaskIds  Danh sách ID theo thứ tự mới
     * @param userEmail   Email người thực hiện
     */
    void reorderSubTasks(Long taskId, List<Long> subtaskIds, String userEmail);
}
