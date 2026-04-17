package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.usecases.dashboard.GetDashboardStatsUseCase;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.dashboard.DashboardStatsResponse;
import com.example.task_management.interfaces.mappers.DashboardStatsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cho Dashboard Statistics
 */
@RestController
@RequestMapping("/api/users/me")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final GetDashboardStatsUseCase getDashboardStatsUseCase;
    private final DashboardStatsMapper dashboardStatsMapper;

    public DashboardController(GetDashboardStatsUseCase getDashboardStatsUseCase,
                               DashboardStatsMapper dashboardStatsMapper) {
        this.getDashboardStatsUseCase = getDashboardStatsUseCase;
        this.dashboardStatsMapper = dashboardStatsMapper;
    }

    /**
     * 10.1 Get User Dashboard Stats
     * GET /api/users/me/dashboard-stats
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(
            Authentication authentication) {

        log.info("[API] Get dashboard stats - user={}", authentication.getName());

        var result = getDashboardStatsUseCase.getDashboardStats(authentication.getName());
        var response = dashboardStatsMapper.toResponse(result);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Lấy thống kê dashboard thành công", response));
    }
}
