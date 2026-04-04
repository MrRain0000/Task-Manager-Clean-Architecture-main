package com.example.task_management.interfaces.exceptions;

/**
 * Exception khi dữ liệu cập nhật project không hợp lệ theo business rules.
 * Trả về HTTP 400 BAD REQUEST.
 */
public class ProjectValidationException extends RuntimeException {
    public ProjectValidationException(String message) {
        super(message);
    }
}
