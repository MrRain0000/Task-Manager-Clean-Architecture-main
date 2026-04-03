package com.example.task_management.application.usecases.task;

import com.example.task_management.application.DTOUsecase.response.task.TaskDetailResult;

/**
 * Use case lấy thông tin chi tiết của một Task.
 * Tuân thủ SRP: Chỉ làm một nhiệm vụ - lấy chi tiết task.
 * Tuân thủ DIP: Depend vào abstraction (interface này), không phụ thuộc implementation.
 */
public interface GetTaskDetailUseCase {

    /**
     * Lấy chi tiết Task bao gồm thông tin cơ bản, project và người được assign.
     *
     * @param projectId ID của dự án chứa task
     * @param taskId    ID của task cần lấy chi tiết
     * @param userEmail Email người dùng đang request (để kiểm tra quyền)
     * @return TaskDetailResult chứa đầy đủ thông tin
     * @throws IllegalArgumentException nếu task không tồn tại hoặc user không có quyền
     */
    TaskDetailResult getTaskDetail(Long projectId, Long taskId, String userEmail);
}
