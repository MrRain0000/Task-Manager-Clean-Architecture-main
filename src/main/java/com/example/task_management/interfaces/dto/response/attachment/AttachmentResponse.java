package com.example.task_management.interfaces.dto.response.attachment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Response cho Attachment API
 */
@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String description;
    private UserInfo uploadedBy;
    private LocalDateTime uploadedAt;
    private Long taskId;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
    }
}
