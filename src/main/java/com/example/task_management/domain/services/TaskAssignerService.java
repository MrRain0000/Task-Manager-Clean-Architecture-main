package com.example.task_management.domain.services;

public interface TaskAssignerService{
    void validateAssignerMembership(Long projectId, Long assignerId);
    void validateAssignee(Long projectId, Long assigneeId);
}
