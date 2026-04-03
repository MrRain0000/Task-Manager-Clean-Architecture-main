package com.example.task_management.application.usecases.task;

/**
 * Use case xóa một Task khỏi dự án.
 * Tuân thủ SRP: Chỉ làm một nhiệm vụ - xóa task.
 * Tuân thủ DIP: Depend vào abstraction (interface này), không phụ thuộc implementation.
 */
public interface DeleteTaskUseCase {

    /**
     * Xóa task khỏi dự án.
     * Chỉ cho phép xóa nếu user là thành viên ACCEPTED của project.
     *
     * @param projectId ID của dự án chứa task
     * @param taskId    ID của task cần xóa
     * @param userEmail Email người dùng đang request (để kiểm tra quyền)
     * @throws IllegalArgumentException nếu task không tồn tại hoặc user không có quyền
     */
    void deleteTask(Long projectId, Long taskId, String userEmail);
}
