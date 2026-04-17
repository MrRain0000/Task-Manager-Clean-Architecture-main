package com.example.task_management.application.repositories.attachment;

import com.example.task_management.domain.entities.Attachment;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho các thao tác đọc (query) Attachment
 */
public interface AttachmentQueryRepository {

    Optional<Attachment> findById(Long id);

    List<Attachment> findAllByTaskId(Long taskId);

    List<Attachment> findAllByTaskIdOrderByUploadedAtDesc(Long taskId);

    int countByTaskId(Long taskId);
}
