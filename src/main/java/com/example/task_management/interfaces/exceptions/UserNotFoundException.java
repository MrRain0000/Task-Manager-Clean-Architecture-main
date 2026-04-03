package com.example.task_management.interfaces.exceptions;

/**
 * Exception khi không tìm thấy User.
 * Trả về HTTP 404 NOT FOUND.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
