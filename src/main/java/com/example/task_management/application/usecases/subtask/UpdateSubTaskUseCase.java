package com.example.task_management.application.usecases.subtask;

import com.example.task_management.application.DTOUsecase.request.subtask.UpdateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;

/**
 * Use case cập nhật SubTask
 */
public interface UpdateSubTaskUseCase {

    /**
     * Cập nhật sub-task
     *
     * @param subtaskId   ID của sub-task
     * @param request     Request chứa thông tin cập nhật
     * @param userEmail   Email người thực hiện
     * @return SubTaskResult chứa thông tin sub-task đã cập nhật
     */
    SubTaskResult updateSubTask(Long subtaskId, UpdateSubTaskRequest request, String userEmail);
}
