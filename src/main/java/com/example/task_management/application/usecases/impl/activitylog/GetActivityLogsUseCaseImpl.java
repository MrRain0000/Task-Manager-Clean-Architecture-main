package com.example.task_management.application.usecases.impl.activitylog;

import com.example.task_management.application.repositories.ActivityLogRepository;
import com.example.task_management.application.DTOUsecase.response.activitylog.ActivityLogResult;
import com.example.task_management.application.usecases.activitylog.GetActivityLogsUseCase;
import com.example.task_management.domain.entities.ActivityLog;
import com.example.task_management.domain.services.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation của GetActivityLogsUseCase.
 * Tuân thủ SRP: Chỉ xử lý logic query logs + permission check.
 * Controller không được check quyền - use case phải điều phối.
 */
@Service
public class GetActivityLogsUseCaseImpl implements GetActivityLogsUseCase {

    private final ActivityLogRepository activityLogRepository;
    private final PermissionService permissionService;

    public GetActivityLogsUseCaseImpl(ActivityLogRepository activityLogRepository, PermissionService permissionService) {
        this.activityLogRepository = activityLogRepository;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResult> getActivityLogs(Long projectId, Pageable pageable, String userEmail) {
        // UC điều phối: Check quyền trước khi query
        permissionService.validateProjectMember(projectId, userEmail);

        // Query logs sau khi đã validate permission
        Page<ActivityLog> logPage = activityLogRepository.findByProjectId(projectId, pageable);
        
        // Map domain entity → Application DTO
        return logPage.map(this::toResult);
    }
    
    private ActivityLogResult toResult(ActivityLog log) {
        return ActivityLogResult.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .metadata(log.getMetadata())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
