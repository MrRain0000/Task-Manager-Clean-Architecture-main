package com.example.task_management.application.usecases.attachment;

import com.example.task_management.application.DTOUsecase.response.attachment.AttachmentResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Use case để upload file đính kèm cho task
 */
public interface UploadAttachmentUseCase {

    /**
     * Upload file đính kèm cho task
     *
     * @param taskId       ID của task
     * @param file         File cần upload
     * @param description  Mô tả file (optional)
     * @param userEmail    Email người dùng đang login
     * @return AttachmentResult chứa thông tin file đã upload
     */
    AttachmentResult uploadAttachment(Long taskId, MultipartFile file, String description, String userEmail);
}
