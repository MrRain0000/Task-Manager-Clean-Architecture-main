package com.example.task_management.domain.entities;

import com.example.task_management.domain.enums.TaskPriority;
import com.example.task_management.domain.enums.TaskStatus;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho công việc con (sub-task) của Task chính
 */
public class SubTask {
    private Long id;
    private Long taskId;
    private String title;
    private String description;
    private Long assigneeId;
    private TaskPriority priority;
    private TaskStatus status;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SubTask() {
        this.status = TaskStatus.TODO;
        this.priority = TaskPriority.MEDIUM;
        this.position = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ── Domain Methods ──────────────────────────────────────────────

    /**
     * Bắt đầu thực hiện sub-task
     */
    public void start() {
        if (this.status != TaskStatus.TODO) {
            throw new IllegalStateException("Chỉ sub-task ở trạng thái TODO mới được bắt đầu");
        }
        this.status = TaskStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Hoàn thành sub-task
     */
    public void complete() {
        if (this.status != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Sub-task phải ở trạng thái IN_PROGRESS trước khi hoàn thành");
        }
        this.status = TaskStatus.DONE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Hủy sub-task
     */
    public void cancel() {
        if (this.status == TaskStatus.DONE) {
            throw new IllegalStateException("Không thể hủy sub-task đã DONE");
        }
        this.status = TaskStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cập nhật thông tin sub-task
     */
    public void update(String title, String description, Long assigneeId, TaskPriority priority, TaskStatus status) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        if (description != null) {
            this.description = description;
        }
        if (assigneeId != null) {
            this.assigneeId = assigneeId;
        }
        if (priority != null) {
            this.priority = priority;
        }
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra sub-task có thuộc task không
     */
    public boolean belongsToTask(Long taskId) {
        return this.taskId != null && this.taskId.equals(taskId);
    }

    /**
     * Cập nhật vị trí (position)
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getPosition() {
        return position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
