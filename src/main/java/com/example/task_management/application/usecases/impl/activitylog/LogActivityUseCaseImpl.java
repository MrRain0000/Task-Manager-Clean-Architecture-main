package com.example.task_management.application.usecases.impl.activitylog;

import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.application.repositories.ActivityLogRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.entities.ActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/*
 * Tuân thủ async requirement: Không block main thread.
 */
@Service
public class LogActivityUseCaseImpl implements LogActivityUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LogActivityUseCaseImpl.class);

    private final ActivityLogRepository activityLogRepository;

    public LogActivityUseCaseImpl(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Async("auditLogExecutor")
    public void logActivity(LogActivityRequest request) {
        try {
            // Bảo vệ: try-catch để không crash business logic nếu logging fail
            ActivityLog log = ActivityLog.builder()
                    .projectId(request.getProjectId())
                    .userId(request.getUserId())
                    .actionType(request.getActionType())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .description(request.getDescription())
                    .metadata(request.getMetadata())
                    .ipAddress(request.getIpAddress())
                    .userAgent(request.getUserAgent())
                    .build();

            activityLogRepository.save(log);
            logger.debug("Activity logged: {} - {} by user {}", 
                    request.getActionType(), request.getEntityType(), request.getUserId());
        } catch (Exception e) {
            // Chỉ log error, không throw exception để tránh ảnh hưởng business logic
            logger.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }
}
