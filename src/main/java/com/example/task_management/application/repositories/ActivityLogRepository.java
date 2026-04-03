package com.example.task_management.application.repositories;

import com.example.task_management.application.repositories.activitylog.ActivityLogCommandRepository;
import com.example.task_management.application.repositories.activitylog.ActivityLogQueryRepository;

/**
 * ActivityLogRepository kế thừa cả Query và Command repositories.
 * Interface này để inject chung, hoặc có thể inject riêng Query/Command khi cần.
 */
public interface ActivityLogRepository extends ActivityLogQueryRepository, ActivityLogCommandRepository {
}
