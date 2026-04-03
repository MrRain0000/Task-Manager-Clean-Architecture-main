package com.example.task_management.interfaces.exceptions;

/**
 * Exception khi user không có quyền truy cập project (không phải thành viên hoặc chưa ACCEPTED).
 * Trả về HTTP 403 FORBIDDEN.
 */
public class ProjectAccessDeniedException extends RuntimeException {
    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
