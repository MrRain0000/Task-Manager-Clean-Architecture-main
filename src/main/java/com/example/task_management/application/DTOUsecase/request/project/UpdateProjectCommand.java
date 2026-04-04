package com.example.task_management.application.DTOUsecase.request.project;

public class UpdateProjectCommand {
    private String name;
    private String description;

    public UpdateProjectCommand() {
    }

    public UpdateProjectCommand(String name, String description) {
        this.name = name;
        this.description = description;
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
}
