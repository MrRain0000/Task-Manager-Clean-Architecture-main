package com.example.task_management.infrastructure.event;

import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.application.events.ProjectUpdatedEvent;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener xử lý các event liên quan đến Project.
 * Chịu trách nhiệm ghi log hoạt động khi project được cập nhật.
 */
@Component
public class ProjectEventListener {

    private final LogActivityUseCase logActivityUseCase;

    public ProjectEventListener(LogActivityUseCase logActivityUseCase) {
        this.logActivityUseCase = logActivityUseCase;
    }

    @EventListener
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        logActivityUseCase.logActivity(LogActivityRequest.builder()
                .projectId(event.getProjectId())
                .userId(event.getUserId())
                .actionType(ActionType.PROJECT_UPDATED)
                .entityType(EntityType.PROJECT)
                .entityId(event.getProjectId())
                .description("Updated project: " + event.getNewName())
                .metadata(Map.of(
                        "oldName", event.getOldName() == null ? "" : event.getOldName(),
                        "newName", event.getNewName(),
                        "oldDescription", event.getOldDescription() == null ? "" : event.getOldDescription(),
                        "newDescription", event.getNewDescription() == null ? "" : event.getNewDescription()
                ))
                .build());
    }
}
