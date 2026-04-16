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
public class MoveTaskResponse {

    private boolean sameColumn;

    private List<TaskResult> affectedTasks;

    private List<TaskResult> allTasks;
}
