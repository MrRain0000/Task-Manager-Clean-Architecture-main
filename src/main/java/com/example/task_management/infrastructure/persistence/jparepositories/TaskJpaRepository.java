package com.example.task_management.infrastructure.persistence.jparepositories;

import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.infrastructure.persistence.jpaentities.TaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long> {
    List<TaskJpaEntity> findAllByProjectId(Long projectId);
    List<TaskJpaEntity> findAllByProjectIdOrderByPositionAsc(Long projectId);
    List<TaskJpaEntity> findAllByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status);
    List<TaskJpaEntity> findAllByProjectIdAndStatusAndPositionGreaterThanEqualOrderByPositionAsc(Long projectId, TaskStatus status, Integer position);
    void deleteAllByProjectId(Long projectId);
    int countByProjectId(Long projectId);
    int countByAssigneeId(Long assigneeId);
    
    // Search by keyword in title or description (case-insensitive)
    @Query("SELECT t FROM TaskJpaEntity t WHERE t.projectId = :projectId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TaskJpaEntity> searchByProjectIdAndKeyword(@Param("projectId") Long projectId, @Param("keyword") String keyword);

    // ==================== BULK UPDATE METHODS FOR KANBAN MOVE ====================

    /**
     * Bulk update: Tăng position cho tasks trong range [startPosition, endPosition]
     * Dùng khi move task xuống trong cùng cột
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskJpaEntity t SET t.position = t.position + 1 " +
           "WHERE t.projectId = :projectId AND t.status = :status " +
           "AND t.position >= :startPosition AND t.position <= :endPosition")
    int bulkIncrementPositionInRange(@Param("projectId") Long projectId,
                                      @Param("status") TaskStatus status,
                                      @Param("startPosition") Integer startPosition,
                                      @Param("endPosition") Integer endPosition);

    /**
     * Bulk update: Giảm position cho tasks trong range [startPosition, endPosition]
     * Dùng khi move task lên trong cùng cột
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskJpaEntity t SET t.position = t.position - 1 " +
           "WHERE t.projectId = :projectId AND t.status = :status " +
           "AND t.position >= :startPosition AND t.position <= :endPosition")
    int bulkDecrementPositionInRange(@Param("projectId") Long projectId,
                                      @Param("status") TaskStatus status,
                                      @Param("startPosition") Integer startPosition,
                                      @Param("endPosition") Integer endPosition);

    /**
     * Bulk update: Giảm position cho tasks phía sau vị trí bị xóa
     * Dùng khi remove task khỏi source column
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskJpaEntity t SET t.position = t.position - 1 " +
           "WHERE t.projectId = :projectId AND t.status = :status " +
           "AND t.position > :removedPosition")
    int bulkDecrementPositionAfter(@Param("projectId") Long projectId,
                                    @Param("status") TaskStatus status,
                                    @Param("removedPosition") Integer removedPosition);

    /**
     * Bulk update: Tăng position cho tasks từ vị trí insert trở đi
     * Dùng khi insert task vào target column
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskJpaEntity t SET t.position = t.position + 1 " +
           "WHERE t.projectId = :projectId AND t.status = :status " +
           "AND t.position >= :insertPosition")
    int bulkIncrementPositionFrom(@Param("projectId") Long projectId,
                                 @Param("status") TaskStatus status,
                                 @Param("insertPosition") Integer insertPosition);

    /**
     * Query tasks theo projectId và status
     * Dùng để lấy lại affected columns sau bulk update
     */
    List<TaskJpaEntity> findByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status);
}
