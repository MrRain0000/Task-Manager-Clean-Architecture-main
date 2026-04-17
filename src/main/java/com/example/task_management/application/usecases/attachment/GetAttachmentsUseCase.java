package com.example.task_management.application.usecases.attachment;

import com.example.task_management.application.DTOUsecase.response.attachment.AttachmentResult;

import java.util.List;

/**
 * Use case để lấy danh sách file đính kèm của task
 */
public interface GetAttachmentsUseCase {

    /**
     * Lấy danh sách file đính kèm của task
     *
     * @param taskId    ID của task
     * @param userEmail Email người dùng đang login
     * @return Danh sách AttachmentResult, sắp xếp theo uploadedAt giảm dần
     */
    List<AttachmentResult> getAttachmentsByTaskId(Long taskId, String userEmail);
}
