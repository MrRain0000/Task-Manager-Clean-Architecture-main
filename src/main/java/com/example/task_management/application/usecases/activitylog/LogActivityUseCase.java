package com.example.task_management.application.usecases.activitylog;

import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;

/**
 * Use case ghi log hoạt động (Audit Logging).
 * Tuân thủ SRP: Chỉ làm nhiệm vụ ghi log.
 * Implementation phải là async (@Async) để không block main thread.
 */
public interface LogActivityUseCase {

    /**
     * Ghi log hoạt động.
     * Method này được thiết kế để chạy bất đồng bộ.
     *
     * @param request Thông tin log cần ghi
     */
    void logActivity(LogActivityRequest request);
}
