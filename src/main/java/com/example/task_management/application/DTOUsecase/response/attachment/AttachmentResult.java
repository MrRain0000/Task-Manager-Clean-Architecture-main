package com.example.task_management.application.DTOUsecase.response.attachment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentResult {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String description;
    private Long uploadedBy;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
    private Long taskId;
}
