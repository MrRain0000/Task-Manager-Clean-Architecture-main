package com.example.task_management.application.usecases.subtask;

import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.domain.enums.TaskStatus;

import java.util.List;

/**
 * Use case lấy danh sách SubTasks
 */
public interface GetSubTasksUseCase {

    /**
     * Lấy tất cả sub-tasks của một task
     *
     * @param taskId      ID của task chính
     * @param userEmail   Email người thực hiện
     * @return Danh sách SubTaskResult
     */
    List<SubTaskResult> getSubTasksByTaskId(Long taskId, String userEmail);

    /**
     * Lấy sub-tasks theo status
     *
     * @param taskId      ID của task chính
     * @param status      Status cần filter
     * @param userEmail   Email người thực hiện
     * @return Danh sách SubTaskResult đã filter
     */
    List<SubTaskResult> getSubTasksByTaskIdAndStatus(Long taskId, TaskStatus status, String userEmail);
}
