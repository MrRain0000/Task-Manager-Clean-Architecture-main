package com.example.task_management.infrastructure.persistence.adapters;

import com.example.task_management.application.repositories.ActivityLogRepository;
import com.example.task_management.domain.entities.ActivityLog;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.infrastructure.persistence.jpaentities.ActivityLogJpaEntity;
import com.example.task_management.infrastructure.persistence.jparepositories.ActivityLogJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter chuyển đổi giữa Domain Entity và JPA Entity cho ActivityLog.
 * Tuân thủ DIP: Application layer phụ thuộc vào interface, implementation ở infrastructure.
 */
@Component
public class ActivityLogRepositoryAdapter implements ActivityLogRepository {

    private final ActivityLogJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public ActivityLogRepositoryAdapter(ActivityLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // Hỗ trợ Java 8 date/time
    }

    @Override
    public ActivityLog save(ActivityLog log) {
        ActivityLogJpaEntity entity = toJpaEntity(log);
        ActivityLogJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ActivityLog> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<ActivityLog> findByProjectId(Long projectId, Pageable pageable) {
        return jpaRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<ActivityLog> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<ActivityLog> findByActionType(ActionType actionType, Pageable pageable) {
        return jpaRepository.findByActionTypeOrderByCreatedAtDesc(actionType, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<ActivityLog> findByProjectIdAndCreatedAtBetween(Long projectId, LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findByProjectIdAndCreatedAtBetweenOrderByCreatedAtDesc(projectId, from, to)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId) {
        return jpaRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── Mappers ─────────────────────────────────────────────────────
    private ActivityLogJpaEntity toJpaEntity(ActivityLog log) {
        return ActivityLogJpaEntity.builder()
                .id(log.getId())
                .projectId(log.getProjectId())
                .userId(log.getUserId())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .metadata(serializeMetadata(log.getMetadata()))
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private ActivityLog toDomain(ActivityLogJpaEntity entity) {
        return ActivityLog.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .userId(entity.getUserId())
                .actionType(entity.getActionType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .metadata(deserializeMetadata(entity.getMetadata()))
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            // Log error nhưng không throw - tránh crash business logic
            System.err.println("Failed to serialize metadata: " + e.getMessage());
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadata, HashMap.class);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to deserialize metadata: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
