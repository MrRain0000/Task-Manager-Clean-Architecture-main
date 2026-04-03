package com.example.task_management.application.usecases.activitylog;

import com.example.task_management.application.DTOUsecase.response.activitylog.ActivityLogResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Use case lấy danh sách activity logs của một project.
 * Tuân thủ SRP: Chỉ xử lý logic query logs.
 * Bao gồm permission check: User phải là member của project.
 */
public interface GetActivityLogsUseCase {

    /**
     * Lấy activity logs của project với pagination.
     * Tự động validate user là member của project.
     *
     * @param projectId ID của project
     * @param pageable Thông tin pagination
     * @param userEmail Email của user đang request
     * @return Page<ActivityLogResult>
     */
    Page<ActivityLogResult> getActivityLogs(Long projectId, Pageable pageable, String userEmail);
}
