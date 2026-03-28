package com.example.task_management.application.dto.request.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignTaskRequest {

    private Long assigneeId;  // null = unassign
}
