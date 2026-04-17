package com.example.task_management.application.usecases.impl.attachment;

import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.attachment.AttachmentCommandRepository;
import com.example.task_management.application.repositories.attachment.AttachmentQueryRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.application.usecases.attachment.DeleteAttachmentUseCase;
import com.example.task_management.domain.entities.Attachment;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import com.example.task_management.domain.enums.MemberRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class DeleteAttachmentUseCaseImpl implements DeleteAttachmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteAttachmentUseCaseImpl.class);

    private final AttachmentCommandRepository attachmentCommandRepository;
    private final AttachmentQueryRepository attachmentQueryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final LogActivityUseCase logActivityUseCase;

    @Value("${app.upload.dir:./uploads/attachments}")
    private String uploadDir;

    public DeleteAttachmentUseCaseImpl(
            AttachmentCommandRepository attachmentCommandRepository,
            AttachmentQueryRepository attachmentQueryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            LogActivityUseCase logActivityUseCase) {
        this.attachmentCommandRepository = attachmentCommandRepository;
        this.attachmentQueryRepository = attachmentQueryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.logActivityUseCase = logActivityUseCase;
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, String userEmail) {
        log.info("[DeleteAttachment] Bắt đầu - attachmentId={}, user={}", attachmentId, userEmail);

        // 1. Validate attachment tồn tại
        Attachment attachment = attachmentQueryRepository.findById(attachmentId)
                .orElseThrow(() -> {
                    log.error("[DeleteAttachment] Attachment không tồn tại: attachmentId={}", attachmentId);
                    return new IllegalArgumentException("File đính kèm không tồn tại");
                });

        // 2. Validate task tồn tại
        Task task = taskRepository.findById(attachment.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task không tồn tại"));

        // 3. Validate user tồn tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 4. Kiểm tra quyền xóa: người upload HOẶC OWNER
        boolean canDelete = canDeleteAttachment(attachment, task.getProjectId(), user);
        if (!canDelete) {
            log.error("[DeleteAttachment] User {} không có quyền xóa attachment {}", user.getId(), attachmentId);
            throw new IllegalArgumentException("Bạn không có quyền xóa file này. Chỉ người upload hoặc OWNER mới có thể xóa.");
        }

        // 5. Xóa file vật lý từ disk
        deleteFileFromDisk(attachment.getFileUrl());

        // 6. Xóa record từ database
        attachmentCommandRepository.deleteById(attachmentId);
        log.info("[DeleteAttachment] Hoàn thành - attachmentId={} đã bị xóa", attachmentId);

        // 7. Ghi log hoạt động (async)
        logActivityUseCase.logActivity(LogActivityRequest.builder()
                .projectId(task.getProjectId())
                .userId(user.getId())
                .actionType(ActionType.ATTACHMENT_DELETED)
                .entityType(EntityType.ATTACHMENT)
                .entityId(attachmentId)
                .description("Deleted file: " + attachment.getFileName())
                .metadata(Map.of(
                        "taskId", attachment.getTaskId(),
                        "fileName", attachment.getFileName(),
                        "deletedBy", user.getId()
                ))
                .build());
    }

    private boolean canDeleteAttachment(Attachment attachment, Long projectId, User user) {
        // Người upload có quyền xóa
        if (attachment.getUploadedBy().equals(user.getId())) {
            return true;
        }

        // OWNER của project có quyền xóa
        return projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .map(ProjectMember::getRole)
                .map(role -> role == MemberRole.OWNER)
                .orElse(false);
    }

    private void deleteFileFromDisk(String fileUrl) {
        try {
            Path filePath = Paths.get(uploadDir, fileUrl);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("[DeleteAttachment] File đã xóa từ disk: {}", filePath);
            } else {
                log.warn("[DeleteAttachment] File không tồn tại trên disk: {}", filePath);
            }
        } catch (IOException e) {
            log.error("[DeleteAttachment] Lỗi khi xóa file từ disk: {}", e.getMessage(), e);
            // Không throw exception vì DB record vẫn cần bị xóa
        }
    }
}
