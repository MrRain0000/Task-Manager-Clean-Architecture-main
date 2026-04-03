package com.example.task_management.application.DTOUsecase.request;

import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;

import java.util.Map;

/**
 * DTO Request cho việc ghi log hoạt động.
 * Sử dụng Builder pattern để tạo request linh hoạt.
 */
public class LogActivityRequest {

    private final Long projectId;
    private final Long userId;
    private final ActionType actionType;
    private final EntityType entityType;
    private final Long entityId;
    private final String description;
    private final Map<String, Object> metadata;
    private final String ipAddress;
    private final String userAgent;

    private LogActivityRequest(Builder builder) {
        this.projectId = builder.projectId;
        this.userId = builder.userId;
        this.actionType = builder.actionType;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.description = builder.description;
        this.metadata = builder.metadata;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
    }

    public Long getProjectId() { return projectId; }
    public Long getUserId() { return userId; }
    public ActionType getActionType() { return actionType; }
    public EntityType getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getDescription() { return description; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long projectId;
        private Long userId;
        private ActionType actionType;
        private EntityType entityType;
        private Long entityId;
        private String description;
        private Map<String, Object> metadata;
        private String ipAddress;
        private String userAgent;

        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder actionType(ActionType actionType) { this.actionType = actionType; return this; }
        public Builder entityType(EntityType entityType) { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId) { this.entityId = entityId; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }

        public LogActivityRequest build() {
            return new LogActivityRequest(this);
        }
    }
}
