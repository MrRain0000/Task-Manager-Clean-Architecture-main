package com.example.task_management.application.usecases.subtask;

/**
 * Use case xóa SubTask
 */
public interface DeleteSubTaskUseCase {

    /**
     * Xóa sub-task
     *
     * @param subtaskId   ID của sub-task cần xóa
     * @param userEmail   Email người thực hiện
     */
    void deleteSubTask(Long subtaskId, String userEmail);
}
