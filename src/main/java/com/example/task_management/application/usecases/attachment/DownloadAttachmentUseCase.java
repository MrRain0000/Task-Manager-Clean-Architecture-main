package com.example.task_management.application.usecases.attachment;

/**
 * Use case để download file đính kèm
 */
public interface DownloadAttachmentUseCase {

    /**
     * Download file đính kèm
     *
     * @param attachmentId ID của file đính kèm cần download
     * @param userEmail    Email người dùng đang login
     * @return FileResource chứa thông tin file và dữ liệu binary
     */
    FileResource downloadAttachment(Long attachmentId, String userEmail);

    /**
     * Data class chứa thông tin file để download
     */
    record FileResource(byte[] data, String fileName, String contentType) {}
}
