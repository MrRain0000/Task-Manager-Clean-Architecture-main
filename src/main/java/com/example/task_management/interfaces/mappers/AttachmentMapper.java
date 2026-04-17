package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.attachment.AttachmentResult;
import com.example.task_management.interfaces.dto.response.attachment.AttachmentResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi giữa AttachmentResult (Application) và AttachmentResponse (Interface)
 */
@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(AttachmentResult result) {
        if (result == null) {
            return null;
        }

        return AttachmentResponse.builder()
                .id(result.getId())
                .fileName(result.getFileName())
                .fileType(result.getFileType())
                .fileSize(result.getFileSize())
                .fileUrl(result.getFileUrl())
                .description(result.getDescription())
                .uploadedBy(AttachmentResponse.UserInfo.builder()
                        .id(result.getUploadedBy())
                        .username(result.getUploadedByUsername())
                        .build())
                .uploadedAt(result.getUploadedAt())
                .taskId(result.getTaskId())
                .build();
    }
}
