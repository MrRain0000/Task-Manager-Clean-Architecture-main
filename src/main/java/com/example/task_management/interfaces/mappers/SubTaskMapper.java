package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.interfaces.dto.response.subtask.SubTaskResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper chuyển đổi giữa SubTaskResult (Application) và SubTaskResponse (Interface)
 */
@Component
public class SubTaskMapper {

    public SubTaskResponse toResponse(SubTaskResult result) {
        if (result == null) {
            return null;
        }

        return SubTaskResponse.builder()
                .id(result.getId())
                .taskId(result.getTaskId())
                .title(result.getTitle())
                .description(result.getDescription())
                .assignee(buildAssigneeInfo(result.getAssigneeId(), result.getAssigneeName()))
                .priority(result.getPriority())
                .status(result.getStatus())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }

    public List<SubTaskResponse> toResponseList(List<SubTaskResult> results) {
        if (results == null) {
            return null;
        }
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SubTaskResponse.AssigneeInfo buildAssigneeInfo(Long assigneeId, String assigneeName) {
        if (assigneeId == null) {
            return null;
        }
        return SubTaskResponse.AssigneeInfo.builder()
                .id(assigneeId)
                .name(assigneeName)
                .build();
    }
}
