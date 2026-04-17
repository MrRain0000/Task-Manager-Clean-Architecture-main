package com.example.task_management.infrastructure.persistence.adapters;

import com.example.task_management.application.repositories.attachment.AttachmentCommandRepository;
import com.example.task_management.application.repositories.attachment.AttachmentQueryRepository;
import com.example.task_management.domain.entities.Attachment;
import com.example.task_management.infrastructure.persistence.jpaentities.AttachmentJpaEntity;
import com.example.task_management.infrastructure.persistence.jparepositories.AttachmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AttachmentRepositoryAdapter implements AttachmentCommandRepository, AttachmentQueryRepository {

    private final AttachmentJpaRepository attachmentJpaRepository;

    public AttachmentRepositoryAdapter(AttachmentJpaRepository attachmentJpaRepository) {
        this.attachmentJpaRepository = attachmentJpaRepository;
    }

    // ── Command Methods ────────────────────────────────────────────

    @Override
    public Attachment save(Attachment attachment) {
        AttachmentJpaEntity entity = toJpaEntity(attachment);
        AttachmentJpaEntity saved = attachmentJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        attachmentJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByTaskId(Long taskId) {
        attachmentJpaRepository.deleteAllByTaskId(taskId);
    }

    // ── Query Methods ──────────────────────────────────────────────

    @Override
    public Optional<Attachment> findById(Long id) {
        return attachmentJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Attachment> findAllByTaskId(Long taskId) {
        return attachmentJpaRepository.findAllByTaskId(taskId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Attachment> findAllByTaskIdOrderByUploadedAtDesc(Long taskId) {
        return attachmentJpaRepository.findAllByTaskIdOrderByUploadedAtDesc(taskId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public int countByTaskId(Long taskId) {
        return attachmentJpaRepository.countByTaskId(taskId);
    }

    // ── Mappers ─────────────────────────────────────────────────────

    private AttachmentJpaEntity toJpaEntity(Attachment attachment) {
        AttachmentJpaEntity entity = new AttachmentJpaEntity();
        if (attachment.getId() != null) entity.setId(attachment.getId());
        entity.setTaskId(attachment.getTaskId());
        entity.setFileName(attachment.getFileName());
        entity.setFileType(attachment.getFileType());
        entity.setFileSize(attachment.getFileSize());
        entity.setFileUrl(attachment.getFileUrl());
        entity.setDescription(attachment.getDescription());
        entity.setUploadedBy(attachment.getUploadedBy());
        entity.setUploadedAt(attachment.getUploadedAt());
        return entity;
    }

    private Attachment toDomain(AttachmentJpaEntity entity) {
        Attachment attachment = new Attachment();
        attachment.setId(entity.getId());
        attachment.setTaskId(entity.getTaskId());
        attachment.setFileName(entity.getFileName());
        attachment.setFileType(entity.getFileType());
        attachment.setFileSize(entity.getFileSize());
        attachment.setFileUrl(entity.getFileUrl());
        attachment.setDescription(entity.getDescription());
        attachment.setUploadedBy(entity.getUploadedBy());
        attachment.setUploadedAt(entity.getUploadedAt());
        return attachment;
    }
}
