package com.example.task_management.application.usecases.impl.attachment;

import com.example.task_management.application.DTOUsecase.response.attachment.AttachmentResult;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.attachment.AttachmentQueryRepository;
import com.example.task_management.application.usecases.attachment.GetAttachmentsUseCase;
import com.example.task_management.domain.entities.Attachment;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.services.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAttachmentsUseCaseImpl implements GetAttachmentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAttachmentsUseCaseImpl.class);

    private final AttachmentQueryRepository attachmentQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public GetAttachmentsUseCaseImpl(
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
    public List<AttachmentResult> getAttachmentsByTaskId(Long taskId, String userEmail) {
        log.info("[GetAttachments] Bắt đầu - taskId={}, user={}", taskId, userEmail);

        // 1. Validate task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[GetAttachments] Task không tồn tại: taskId={}", taskId);
                    return new IllegalArgumentException("Task không tồn tại");
                });

        // 2. Validate user tồn tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 3. Validate user là thành viên ACCEPTED của project
        permissionService.validateProjectMember(task.getProjectId(), userEmail);
        log.debug("[GetAttachments] User {} có quyền xem attachments của task {}", user.getId(), taskId);

        // 4. Lấy danh sách attachments
        List<Attachment> attachments = attachmentQueryRepository.findAllByTaskIdOrderByUploadedAtDesc(taskId);
        log.info("[GetAttachments] Hoàn thành - tìm thấy {} attachments cho taskId={}", attachments.size(), taskId);

        // 5. Map sang result
        return attachments.stream()
                .map(this::mapToResult)
                .toList();
    }

    private AttachmentResult mapToResult(Attachment attachment) {
        // Lấy username của người upload
        String uploadedByUsername = userRepository.findById(attachment.getUploadedBy())
                .map(User::getUsername)
                .orElse("Unknown");

        return AttachmentResult.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .fileUrl(attachment.getFileUrl())
                .description(attachment.getDescription())
                .uploadedBy(attachment.getUploadedBy())
                .uploadedByUsername(uploadedByUsername)
                .uploadedAt(attachment.getUploadedAt())
                .taskId(attachment.getTaskId())
                .build();
    }
}
