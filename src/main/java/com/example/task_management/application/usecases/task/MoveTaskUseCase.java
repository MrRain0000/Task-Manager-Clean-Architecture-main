package com.example.task_management.application.usecases.task;

import com.example.task_management.application.dto.request.task.MoveTaskRequest;
import com.example.task_management.application.dto.response.task.TaskResponse;

public interface MoveTaskUseCase {

    /**
     * Di chuyển Task trong Kanban board (cùng column hoặc khác column)
     *
     * @param projectId  ID của project chứa Task
     * @param taskId     ID của Task cần di chuyển
     * @param request    Payload: toStatus, toPosition
     * @param userEmail  Email người dùng đang login (lấy từ JWT)
     * @return TaskResponse chứa thông tin Task sau khi di chuyển
     */
    TaskResponse moveTask(Long projectId, Long taskId, MoveTaskRequest request, String userEmail);

}
