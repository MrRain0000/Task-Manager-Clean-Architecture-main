package com.example.task_management.application.usecases.task;

import com.example.task_management.interfaces.dto.request.task.MoveTaskRequest;
import com.example.task_management.application.DTOUsecase.response.task.MoveTaskResponse;

public interface MoveTaskUseCase {

    /**
     * Di chuyển Task trong Kanban board (cùng column hoặc khác column)
     * Trả về Smart Response: affectedTasks nếu same column, allTasks nếu different column
     *
     * @param projectId  ID của project chứa Task
     * @param taskId     ID của Task cần di chuyển
     * @param request    Payload: toStatus, toPosition
     * @param userEmail  Email người dùng đang login (lấy từ JWT)
     * @return MoveTaskResponse chứa affectedTasks hoặc allTasks tùy trường hợp
     */
    MoveTaskResponse moveTask(Long projectId, Long taskId, MoveTaskRequest request, String userEmail);

}
