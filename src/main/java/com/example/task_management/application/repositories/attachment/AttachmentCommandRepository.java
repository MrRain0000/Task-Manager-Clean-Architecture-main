package com.example.task_management.application.repositories.attachment;

import com.example.task_management.domain.entities.Attachment;

/**
 * Repository cho các thao tác ghi (command) Attachment
 */
public interface AttachmentCommandRepository {

    Attachment save(Attachment attachment);

    void deleteById(Long id);

    void deleteAllByTaskId(Long taskId);
}
