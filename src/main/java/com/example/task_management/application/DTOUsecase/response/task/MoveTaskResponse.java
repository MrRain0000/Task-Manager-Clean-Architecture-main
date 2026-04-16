package com.example.task_management.application.DTOUsecase.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response cho Move Task với Incremental Sync.
 * Chỉ trả về các columns bị ảnh hưởng, không trả toàn bộ project tasks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveTaskResponse {

    /**
     * Map của các columns bị ảnh hưởng.
     * Key: status name (TODO, IN_PROGRESS, DONE, CANCELLED)
     * Value: List tasks trong column đó (đã sort by position)
     *
     * Examples:
     * - Move trong cùng column: {"TODO": [...]}
     * - Move khác column: {"TODO": [...], "IN_PROGRESS": [...]}
     */
    private Map<String, List<TaskResult>> affectedColumns;
}
