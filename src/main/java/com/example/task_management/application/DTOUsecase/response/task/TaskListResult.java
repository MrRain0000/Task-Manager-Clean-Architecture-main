package com.example.task_management.application.DTOUsecase.response.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResult {
    private List<TaskResult> tasks;
    private int totalCount;
}
