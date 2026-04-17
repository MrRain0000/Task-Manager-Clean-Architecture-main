package com.example.task_management.application.DTOUsecase.request.subtask;

import lombok.Data;

import java.util.List;

/**
 * Request DTO cho sắp xếp lại SubTasks
 */
@Data
public class ReorderSubTasksRequest {
    private List<Long> subtaskIds;
}
