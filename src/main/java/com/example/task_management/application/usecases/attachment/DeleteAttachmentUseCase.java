package com.example.task_management.application.usecases.attachment;

/**
 * Use case để xóa file đính kèm
 */
public interface DeleteAttachmentUseCase {

    /**
     * Xóa file đính kèm
     *
     * @param attachmentId ID của file đính kèm cần xóa
     * @param userEmail    Email người dùng đang login
     */
    void deleteAttachment(Long attachmentId, String userEmail);
}
