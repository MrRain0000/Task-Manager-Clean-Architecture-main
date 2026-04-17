package com.example.task_management.application.usecases.subtask;

import com.example.task_management.application.DTOUsecase.request.subtask.CreateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;

/**
 * Use case tạo mới SubTask
 */
public interface CreateSubTaskUseCase {

    /**
     * Tạo sub-task mới cho task
     *
     * @param taskId      ID của task chính
     * @param request     Request chứa thông tin sub-task
     * @param userEmail   Email người thực hiện
     * @return SubTaskResult chứa thông tin sub-task đã tạo
     */
    SubTaskResult createSubTask(Long taskId, CreateSubTaskRequest request, String userEmail);
}
