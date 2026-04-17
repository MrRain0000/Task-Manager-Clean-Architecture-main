package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.response.attachment.AttachmentResult;
import com.example.task_management.application.usecases.attachment.DeleteAttachmentUseCase;
import com.example.task_management.application.usecases.attachment.DownloadAttachmentUseCase;
import com.example.task_management.application.usecases.attachment.GetAttachmentsUseCase;
import com.example.task_management.application.usecases.attachment.UploadAttachmentUseCase;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.attachment.AttachmentResponse;
import com.example.task_management.interfaces.mappers.AttachmentMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST Controller cho Attachment (File đính kèm Task)
 */
@RestController
@RequestMapping("/api")
public class AttachmentController {

    private static final Logger log = LoggerFactory.getLogger(AttachmentController.class);

    private final UploadAttachmentUseCase uploadAttachmentUseCase;
    private final GetAttachmentsUseCase getAttachmentsUseCase;
    private final DeleteAttachmentUseCase deleteAttachmentUseCase;
    private final DownloadAttachmentUseCase downloadAttachmentUseCase;
    private final AttachmentMapper attachmentMapper;

    public AttachmentController(
            UploadAttachmentUseCase uploadAttachmentUseCase,
            GetAttachmentsUseCase getAttachmentsUseCase,
            DeleteAttachmentUseCase deleteAttachmentUseCase,
            DownloadAttachmentUseCase downloadAttachmentUseCase,
            AttachmentMapper attachmentMapper) {
        this.uploadAttachmentUseCase = uploadAttachmentUseCase;
        this.getAttachmentsUseCase = getAttachmentsUseCase;
        this.deleteAttachmentUseCase = deleteAttachmentUseCase;
        this.downloadAttachmentUseCase = downloadAttachmentUseCase;
        this.attachmentMapper = attachmentMapper;
    }

    /**
     * 9.1 Upload file đính kèm cho task
     * POST /api/tasks/{taskId}/attachments
     */
    @PostMapping(value = "/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {

        log.info("[API] Upload attachment - taskId={}, fileName={}, user={}",
                taskId, file.getOriginalFilename(), authentication.getName());

        AttachmentResult result = uploadAttachmentUseCase.uploadAttachment(
                taskId, file, description, authentication.getName());

        AttachmentResponse responseData = attachmentMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Upload file thành công", responseData));
    }

    /**
     * 9.2 Lấy danh sách file đính kèm của task
     * GET /api/tasks/{taskId}/attachments
     */
    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(
            @PathVariable Long taskId,
            Authentication authentication) {

        log.info("[API] Get attachments - taskId={}, user={}", taskId, authentication.getName());

        List<AttachmentResult> results = getAttachmentsUseCase.getAttachmentsByTaskId(
                taskId, authentication.getName());

        List<AttachmentResponse> responseData = results.stream()
                .map(attachmentMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách file đính kèm thành công", responseData));
    }

    /**
     * 9.3 Xóa file đính kèm
     * DELETE /api/attachments/{attachmentId}
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long attachmentId,
            Authentication authentication) {

        log.info("[API] Delete attachment - attachmentId={}, user={}", attachmentId, authentication.getName());

        deleteAttachmentUseCase.deleteAttachment(attachmentId, authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Xóa file thành công", null));
    }

    /**
     * 9.4 Download file đính kèm
     * GET /api/attachments/{attachmentId}/download
     */
    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId,
            Authentication authentication,
            HttpServletRequest request) {

        log.info("[API] Download attachment - attachmentId={}, user={}", attachmentId, authentication.getName());

        DownloadAttachmentUseCase.FileResource fileResource = downloadAttachmentUseCase.downloadAttachment(
                attachmentId, authentication.getName());

        // Encode filename để hỗ trợ Unicode trong Content-Disposition
        String encodedFilename = URLEncoder.encode(fileResource.fileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        // Xác định Content-Type
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(fileResource.contentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // Build response với headers cho download
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; " +
                                "filename*=UTF-8''" + encodedFilename)
                .body(new ByteArrayResource(fileResource.data()));
    }
}
