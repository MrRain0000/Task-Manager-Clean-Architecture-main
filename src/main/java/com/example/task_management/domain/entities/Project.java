package com.example.task_management.domain.entities;

import com.example.task_management.interfaces.exceptions.ProjectValidationException;
import java.time.LocalDate;

public class Project {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDate deadline;

    public Project() {}

    public String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProjectValidationException("Tên dự án không được để trống.");
        }

        String normalizedName = name.trim();
        if (normalizedName.length() > 100) {
            throw new ProjectValidationException("Tên dự án không được vượt quá 100 ký tự.");
        }

        return normalizedName;
    }

    public String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        if (normalizedDescription.length() > 500) {
            throw new ProjectValidationException("Mô tả dự án không được vượt quá 500 ký tự.");
        }

        return normalizedDescription;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}
