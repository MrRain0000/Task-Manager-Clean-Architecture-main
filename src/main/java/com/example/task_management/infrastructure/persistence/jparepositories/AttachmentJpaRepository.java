package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.infrastructure.persistence.jpaentities.AttachmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentJpaRepository extends JpaRepository<AttachmentJpaEntity, Long> {

    List<AttachmentJpaEntity> findAllByTaskIdOrderByUploadedAtDesc(Long taskId);

    List<AttachmentJpaEntity> findAllByTaskId(Long taskId);

    void deleteAllByTaskId(Long taskId);

    int countByTaskId(Long taskId);
}
