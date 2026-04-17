package com.example.task_management.domain.entities;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho file đính kèm của Task
 */
public class Attachment {
    private Long id;
    private Long taskId;
    private String fileName;      // Tên file gốc
    private String fileType;      // MIME type (image/png, application/pdf, ...)
    private Long fileSize;        // Kích thước file (bytes)
    private String fileUrl;       // Đường dẫn lưu file trên server (relative path)
    private String description;   // Mô tả file (optional)
    private Long uploadedBy;      // User ID người upload
    private LocalDateTime uploadedAt;

    public Attachment() {
    }

    // ── Domain Methods ──────────────────────────────────────────────

    /**
     * Kiểm tra xem user có quyền xóa file không
     * - Người upload có quyền xóa
     * - OWNER của project cũng có quyền xóa (kiểm tra ở use case)
     */
    public boolean canBeDeletedBy(Long userId) {
        return this.uploadedBy.equals(userId);
    }

    /**
     * Kiểm tra file có thuộc task không
     */
    public boolean belongsToTask(Long taskId) {
        return this.taskId.equals(taskId);
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
