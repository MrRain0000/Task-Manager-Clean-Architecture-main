package com.example.task_management.domain.entities;

import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain Entity cho Audit Log (Activity Log).
 * Immutable - log entries không được sửa/xóa sau khi tạo.
 */
public final class ActivityLog {

    private final Long id;
    private final Long projectId;
    private final Long userId;
    private final ActionType actionType;
    private final EntityType entityType;
    private final Long entityId;
    private final String description;
    private final Map<String, Object> metadata;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime createdAt;

    private ActivityLog(Builder builder) {
        this.id = builder.id;
        this.projectId = Objects.requireNonNull(builder.projectId, "projectId không được null");
        this.userId = Objects.requireNonNull(builder.userId, "userId không được null");
        this.actionType = Objects.requireNonNull(builder.actionType, "actionType không được null");
        this.entityType = Objects.requireNonNull(builder.entityType, "entityType không được null");
        this.entityId = builder.entityId;
        this.description = builder.description;
        this.metadata = builder.metadata != null ? new HashMap<>(builder.metadata) : new HashMap<>();
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public Long getUserId() { return userId; }
    public ActionType getActionType() { return actionType; }
    public EntityType getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getDescription() { return description; }
    public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long projectId;
        private Long userId;
        private ActionType actionType;
        private EntityType entityType;
        private Long entityId;
        private String description;
        private Map<String, Object> metadata;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder actionType(ActionType actionType) { this.actionType = actionType; return this; }
        public Builder entityType(EntityType entityType) { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId) { this.entityId = entityId; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ActivityLog build() {
            return new ActivityLog(this);
        }
    }
}
