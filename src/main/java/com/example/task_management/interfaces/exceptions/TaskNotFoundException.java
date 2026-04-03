package com.example.task_management.interfaces.exceptions;

/**
 * Exception khi không tìm thấy Task.
 * Trả về HTTP 404 NOT FOUND.
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
