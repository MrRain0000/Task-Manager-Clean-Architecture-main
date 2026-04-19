package com.example.task_management.interfaces.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    @NotBlank(message = "Tên dự án không được để trống")
    private String name;
    
    private String description;
    
    private LocalDate dueDate;
}
