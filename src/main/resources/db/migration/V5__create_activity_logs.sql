-- Migration: Create activity_logs table for Audit Logging
-- Date: 2024-01-15

CREATE TABLE IF NOT EXISTS activity_logs (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    project_id      BIGINT          NOT NULL              COMMENT 'Project nơi action xảy ra',
    user_id         BIGINT          NOT NULL              COMMENT 'User thực hiện action',
    action_type     VARCHAR(50)     NOT NULL              COMMENT 'TASK_CREATED, TASK_MOVED, etc.',
    entity_type     VARCHAR(50)     NOT NULL              COMMENT 'TASK, PROJECT, MEMBER',
    entity_id       BIGINT                                  COMMENT 'ID của entity bị tác động',
    description     VARCHAR(500)                            COMMENT 'Mô tả ngắn gọn',
    metadata        JSON                                    COMMENT 'Chi tiết dạng JSON (flexible)',
    ip_address      VARCHAR(45)                             COMMENT 'IP của user',
    user_agent      VARCHAR(500)                            COMMENT 'Browser/device info',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Indexes cho query hiệu quả
    INDEX idx_logs_project (project_id, created_at)         COMMENT 'Query theo project + time',
    INDEX idx_logs_user (user_id, created_at)               COMMENT 'Query theo user + time',
    INDEX idx_logs_action (action_type, created_at)         COMMENT 'Query theo action type',
    INDEX idx_logs_entity (entity_type, entity_id)          COMMENT 'Query theo entity',
    
    -- Foreign keys
    CONSTRAINT fk_logs_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
    
) ENGINE = InnoDB 
  DEFAULT CHARSET = utf8mb4 
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Audit logs cho user activities trong projects';
