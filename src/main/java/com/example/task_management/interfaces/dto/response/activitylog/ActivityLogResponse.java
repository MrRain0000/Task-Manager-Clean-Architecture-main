package com.example.task_management.interfaces.dto.response.activitylog;

import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO Response cho ActivityLog.
 * Chứa thông tin hiển thị trên activity feed.
 */
@Data
@Builder
public class ActivityLogResponse {
    
    private Long id;
    private UserInfo user;
    private ActionType actionType;
    private EntityType entityType;
    private Long entityId;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;

    /**
     * Thông tin user thực hiện action (nested object).
     */
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
    }
}
