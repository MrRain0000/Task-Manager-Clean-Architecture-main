package com.example.task_management.application.repositories.activitylog;

import com.example.task_management.domain.entities.ActivityLog;
import com.example.task_management.domain.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho các thao tác đọc (query) ActivityLog.
 * CQS: Tách riêng query từ command.
 */
public interface ActivityLogQueryRepository {

    Optional<ActivityLog> findById(Long id);

    /**
     * Query logs theo project với pagination - tối ưu cho việc hiển thị activity feed.
     */
    Page<ActivityLog> findByProjectId(Long projectId, Pageable pageable);

    /**
     * Query logs theo user với pagination.
     */
    Page<ActivityLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Query logs theo action type với pagination.
     */
    Page<ActivityLog> findByActionType(ActionType actionType, Pageable pageable);

    /**
     * Query logs trong khoảng thời gian.
     */
    List<ActivityLog> findByProjectIdAndCreatedAtBetween(Long projectId, LocalDateTime from, LocalDateTime to);

    /**
     * Query logs theo entity (ví dụ: tất cả actions trên một task).
     */
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
