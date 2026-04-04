package com.example.task_management.application.events;

/**
 * Event được publish khi một project được cập nhật.
 * Chứa thông tin cần thiết để ghi log hoạt động.
 */
public class ProjectUpdatedEvent {

    private final Long projectId;
    private final Long userId;
    private final String newName;
    private final String newDescription;
    private final String oldName;
    private final String oldDescription;

    public ProjectUpdatedEvent(Long projectId, Long userId, 
                               String newName, String newDescription,
                               String oldName, String oldDescription) {
        this.projectId = projectId;
        this.userId = userId;
        this.newName = newName;
        this.newDescription = newDescription;
        this.oldName = oldName;
        this.oldDescription = oldDescription;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public String getOldName() {
        return oldName;
    }

    public String getOldDescription() {
        return oldDescription;
    }
}
