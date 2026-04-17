package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.response.dashboard.DashboardStatsResult;
import com.example.task_management.application.usecases.dashboard.GetDashboardStatsUseCase;
import com.example.task_management.interfaces.dto.response.ApiResponse;
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

    public DashboardController(GetDashboardStatsUseCase getDashboardStatsUseCase) {
        this.getDashboardStatsUseCase = getDashboardStatsUseCase;
    }

    /**
     * 10.1 Get User Dashboard Stats
     * GET /api/users/me/dashboard-stats
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResult>> getDashboardStats(
            Authentication authentication) {

        log.info("[API] Get dashboard stats - user={}", authentication.getName());

        DashboardStatsResult stats = getDashboardStatsUseCase.getDashboardStats(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Lấy thống kê dashboard thành công", stats));
    }
}
