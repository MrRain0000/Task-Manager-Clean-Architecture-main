package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.request.project.UpdateProjectCommand;
import com.example.task_management.interfaces.dto.request.project.UpdateProjectRequest;
import org.springframework.stereotype.Component;

@Component
public class ProjectRequestMapper {

    public UpdateProjectCommand toUpdateProjectCommand(UpdateProjectRequest request) {
        if (request == null) {
            return null;
        }

        return new UpdateProjectCommand(request.getName(), request.getDescription());
    }
}
