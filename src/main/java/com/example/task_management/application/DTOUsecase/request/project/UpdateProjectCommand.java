package com.example.task_management.application.DTOUsecase.request.project;

import java.time.LocalDate;

public class UpdateProjectCommand {
    private String name;
    private String description;
    private LocalDate deadline;

    public UpdateProjectCommand() {
    }

    public UpdateProjectCommand(String name, String description, LocalDate deadline) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
