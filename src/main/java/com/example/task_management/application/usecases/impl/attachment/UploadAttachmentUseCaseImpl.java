package com.example.task_management.application.usecases.impl.attachment;

import com.example.task_management.application.DTOUsecase.request.LogActivityRequest;
import com.example.task_management.application.DTOUsecase.response.attachment.AttachmentResult;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.attachment.AttachmentCommandRepository;
import com.example.task_management.application.repositories.attachment.AttachmentQueryRepository;
import com.example.task_management.application.usecases.activitylog.LogActivityUseCase;
import com.example.task_management.application.usecases.attachment.UploadAttachmentUseCase;
import com.example.task_management.domain.entities.Attachment;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.ActionType;
import com.example.task_management.domain.enums.EntityType;
import com.example.task_management.domain.services.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadAttachmentUseCaseImpl implements UploadAttachmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadAttachmentUseCaseImpl.class);

    // Các MIME type được phép upload
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final AttachmentCommandRepository attachmentCommandRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final LogActivityUseCase logActivityUseCase;

    @Value("${app.upload.dir:./uploads/attachments}")
    private String uploadDir;

    public UploadAttachmentUseCaseImpl(
            AttachmentCommandRepository attachmentCommandRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            PermissionService permissionService,
            LogActivityUseCase logActivityUseCase) {
        this.attachmentCommandRepository = attachmentCommandRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.logActivityUseCase = logActivityUseCase;
    }

    @Override
    @Transactional
    public AttachmentResult uploadAttachment(Long taskId, MultipartFile file, String description, String userEmail) {
        log.info("[UploadAttachment] Bắt đầu - taskId={}, fileName={}, user={}", taskId, file.getOriginalFilename(), userEmail);

        // 1. Validate task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[UploadAttachment] Task không tồn tại: taskId={}", taskId);
                    return new IllegalArgumentException("Task không tồn tại");
                });

        // 2. Validate user tồn tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 3. Validate user là thành viên ACCEPTED của project
        permissionService.validateProjectMember(task.getProjectId(), userEmail);
        log.debug("[UploadAttachment] User {} là thành viên project {}", user.getId(), task.getProjectId());

        // 4. Validate file
        validateFile(file);

        // 5. Lưu file vào disk
        String fileUrl = saveFileToDisk(file);
        log.debug("[UploadAttachment] File đã lưu tại: {}", fileUrl);

        // 6. Tạo attachment entity
        Attachment attachment = new Attachment();
        attachment.setTaskId(taskId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFileUrl(fileUrl);
        attachment.setDescription(description);
        attachment.setUploadedBy(user.getId());
        attachment.setUploadedAt(LocalDateTime.now());

        // 7. Lưu vào database
        Attachment savedAttachment = attachmentCommandRepository.save(attachment);
        log.info("[UploadAttachment] Hoàn thành - attachmentId={}, taskId={}", savedAttachment.getId(), taskId);

        // 8. Ghi log hoạt động (async)
        logActivityUseCase.logActivity(LogActivityRequest.builder()
                .projectId(task.getProjectId())
                .userId(user.getId())
                .actionType(ActionType.ATTACHMENT_UPLOADED)
                .entityType(EntityType.ATTACHMENT)
                .entityId(savedAttachment.getId())
                .description("Uploaded file: " + file.getOriginalFilename())
                .metadata(java.util.Map.of(
                        "taskId", taskId,
                        "fileName", file.getOriginalFilename(),
                        "fileSize", file.getSize()
                ))
                .build());

        return mapToResult(savedAttachment, user.getUsername());
    }

    private void validateFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa là 10MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: hình ảnh, PDF, Word, TXT");
        }

        // Check file name
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
    }

    private String saveFileToDisk(MultipartFile file) {
        try {
            // Tạo đường dẫn theo năm/tháng/ngày
            LocalDateTime now = LocalDateTime.now();
            String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetDir = Paths.get(uploadDir, datePath);

            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(targetDir);

            // Tạo tên file unique với UUID
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Lưu file
            Path targetPath = targetDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath);

            // Trả về relative path
            return datePath + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("[UploadAttachment] Lỗi khi lưu file: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu file: " + e.getMessage(), e);
        }
    }

    private AttachmentResult mapToResult(Attachment attachment, String uploadedByUsername) {
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
