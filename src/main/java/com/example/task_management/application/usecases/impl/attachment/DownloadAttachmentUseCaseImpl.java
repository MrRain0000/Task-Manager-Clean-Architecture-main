package com.example.task_management.application.usecases.impl.attachment;

import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.attachment.AttachmentQueryRepository;
import com.example.task_management.application.usecases.attachment.DownloadAttachmentUseCase;
import com.example.task_management.domain.entities.Attachment;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.services.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DownloadAttachmentUseCaseImpl implements DownloadAttachmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(DownloadAttachmentUseCaseImpl.class);

    private final AttachmentQueryRepository attachmentQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Value("${app.upload.dir:./uploads/attachments}")
    private String uploadDir;

    public DownloadAttachmentUseCaseImpl(
            AttachmentQueryRepository attachmentQueryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            PermissionService permissionService) {
        this.attachmentQueryRepository = attachmentQueryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    @Override
    public FileResource downloadAttachment(Long attachmentId, String userEmail) {
        log.info("[DownloadAttachment] Bắt đầu - attachmentId={}, user={}", attachmentId, userEmail);

        // 1. Validate attachment tồn tại
        Attachment attachment = attachmentQueryRepository.findById(attachmentId)
                .orElseThrow(() -> {
                    log.error("[DownloadAttachment] Attachment không tồn tại: attachmentId={}", attachmentId);
                    return new IllegalArgumentException("File đính kèm không tồn tại");
                });

        // 2. Validate task tồn tại
        Task task = taskRepository.findById(attachment.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task không tồn tại"));

        // 3. Validate user tồn tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 4. Validate user là thành viên ACCEPTED của project
        permissionService.validateProjectMember(task.getProjectId(), userEmail);
        log.debug("[DownloadAttachment] User {} có quyền download attachment {}", user.getId(), attachmentId);

        // 5. Đọc file từ disk
        byte[] fileData = readFileFromDisk(attachment.getFileUrl());
        log.info("[DownloadAttachment] Hoàn thành - attachmentId={}, fileName={}, size={} bytes",
                attachmentId, attachment.getFileName(), fileData.length);

        return new FileResource(
                fileData,
                attachment.getFileName(),
                attachment.getFileType()
        );
    }

    private byte[] readFileFromDisk(String fileUrl) {
        try {
            Path filePath = Paths.get(uploadDir, fileUrl);
            if (!Files.exists(filePath)) {
                log.error("[DownloadAttachment] File không tồn tại trên disk: {}", filePath);
                throw new IllegalArgumentException("File không tồn tại trên server");
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("[DownloadAttachment] Lỗi khi đọc file từ disk: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đọc file: " + e.getMessage(), e);
        }
    }
}
