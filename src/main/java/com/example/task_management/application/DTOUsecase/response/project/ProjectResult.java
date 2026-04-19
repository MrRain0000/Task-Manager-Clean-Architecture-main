package com.example.task_management.application.DTOUsecase.response.project;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResult {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDate deadline;
}
