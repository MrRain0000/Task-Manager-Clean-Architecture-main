package com.example.task_management.interfaces.exceptions;

import com.example.task_management.domain.exceptions.UserNotVerifiedException;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý lỗi validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ", fieldErrors);
    }

    // Xử lý lỗi user chưa xác minh
    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotVerified(UserNotVerifiedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    // Xử lý lỗi business logic (email đã tồn tại, sai mật khẩu...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // Xử lý lỗi không tìm thấy resource (404)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Xử lý lỗi không tìm thấy Task (404)
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleTaskNotFound(TaskNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Xử lý lỗi không tìm thấy Project (404)
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleProjectNotFound(ProjectNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Xử lý lỗi không tìm thấy User (404)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Xử lý lỗi không có quyền truy cập Project (403)
    @ExceptionHandler(ProjectAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleProjectAccessDenied(ProjectAccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    // Xử lý các lỗi hệ thống không mong đợi
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        // Lấy stack trace chi tiết để dev dễ debug
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", ex.getClass().getName());
        errorDetails.put("trace", exceptionAsString); // Cung cấp chi tiết lỗi để dev hiểu

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống", errorDetails);
    }

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(HttpStatus status, String message, Object errors) {
        ApiResponse<Object> response = ApiResponse.error(status.value(), message, errors);
        return ResponseEntity.status(status).body(response);
    }
}
