package com.example.task_management.interfaces.dto.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponse {
    private List<TaskResponse> tasks;
    private int totalCount;
}
