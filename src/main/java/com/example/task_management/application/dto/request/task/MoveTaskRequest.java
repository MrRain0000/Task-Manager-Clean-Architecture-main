package com.example.task_management.application.dto.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveTaskRequest {

    @NotNull(message = "toStatus không được để trống")
    private String toStatus;

    @NotNull(message = "toPosition không được để trống")
    private Integer toPosition;
}
