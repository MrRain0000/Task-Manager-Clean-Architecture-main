package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.usecases.activitylog.GetActivityLogsUseCase;
import com.example.task_management.application.DTOUsecase.response.activitylog.ActivityLogResult;
import com.example.task_management.interfaces.mappers.ActivityLogMapper;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.activitylog.ActivityLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Activity Logs.
 * API: GET /api/projects/{projectId}/activity-logs
 */
@RestController
@RequestMapping("/api/projects/{projectId}/activity-logs")
public class ActivityLogController {

    private final GetActivityLogsUseCase getActivityLogsUseCase;
    private final ActivityLogMapper activityLogMapper;

    public ActivityLogController(GetActivityLogsUseCase getActivityLogsUseCase, ActivityLogMapper activityLogMapper) {
        this.getActivityLogsUseCase = getActivityLogsUseCase;
        this.activityLogMapper = activityLogMapper;
    }

    /**
     * Lấy danh sách activity logs của một project với pagination.
     * 
     * @param projectId ID của project
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 20, max: 100)
     * @param authentication Thông tin user đang login
     * @return Page<ActivityLogResponse>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getActivityLogs(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        // Validate và giới hạn size để tránh query quá nặng
        int validSize = Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, validSize, Sort.by("createdAt").descending());
        
        // UC điều phối: gọi use case thay vì trực tiếp repository
        // Use case sẽ handle permission check
        Page<ActivityLogResult> logPage = getActivityLogsUseCase.getActivityLogs(projectId, pageable, authentication.getName());
        
        // Map Application DTO → Interface DTO (qua mapper)
        Page<ActivityLogResponse> responsePage = logPage.map(activityLogMapper::toResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success(HttpStatus.OK.value(), "Lấy activity logs thành công", responsePage)
        );
    }
}
