package com.example.task_management.interfaces.exceptions;

/**
 * Exception khi không tìm thấy Project.
 * Trả về HTTP 404 NOT FOUND.
 */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
