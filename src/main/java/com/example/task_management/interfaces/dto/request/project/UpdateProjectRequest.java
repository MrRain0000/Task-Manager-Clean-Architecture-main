package com.example.task_management.interfaces.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {
    @NotBlank(message = "Tên dự án không được để trống")
    @Size(max = 100, message = "Tên dự án không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả dự án không được vượt quá 500 ký tự")
    private String description;

    public UpdateProjectRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
