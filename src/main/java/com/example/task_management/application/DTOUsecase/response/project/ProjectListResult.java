package com.example.task_management.application.DTOUsecase.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListResult {
    private List<ProjectResult> projects;
    private int totalProjects;
}
