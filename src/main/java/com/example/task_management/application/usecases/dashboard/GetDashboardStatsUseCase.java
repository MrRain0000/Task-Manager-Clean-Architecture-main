package com.example.task_management.application.usecases.dashboard;

import com.example.task_management.application.DTOUsecase.response.dashboard.DashboardStatsResult;

/**
 * Use case lấy thống kê dashboard tổng hợp từ tất cả projects
 */
public interface GetDashboardStatsUseCase {

    /**
     * Lấy thống kê dashboard cho user hiện tại
     *
     * @param userEmail Email của user đang đăng nhập
     * @return DashboardStatsResult chứa thống kê tổng hợp
     */
    DashboardStatsResult getDashboardStats(String userEmail);
}
