package com.example.task_management.application.DTOUsecase.response.activitylog;

import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO Response cho tầng Application (Use Case).
 * Chứa thông tin cơ bản của ActivityLog để truyền từ UC ra Controller.
 * Controller sẽ chuyển đổi thành ActivityLogResponse cho tầng Interface.
 */
@Data
@Builder
public class ActivityLogResult {
    
    private Long id;
    private Long userId;
    private ActionType actionType;
    private EntityType entityType;
    private Long entityId;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
