package com.example.task_management.application.repositories.activitylog;

import com.example.task_management.domain.entities.ActivityLog;

/**
 * Repository cho các thao tác ghi (command) ActivityLog.
 * CQS: Tách riêng command từ query.
 * Chỉ có save - không có update/delete vì logs là immutable.
 */
public interface ActivityLogCommandRepository {

    /**
     * Lưu activity log mới.
     * @return ActivityLog đã được lưu (có ID được generate)
     */
    ActivityLog save(ActivityLog log);
}
