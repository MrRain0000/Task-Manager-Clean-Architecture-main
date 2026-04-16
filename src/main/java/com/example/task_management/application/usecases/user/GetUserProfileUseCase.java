package com.example.task_management.application.usecases.user;

import com.example.task_management.application.DTOUsecase.response.user.UserProfileResult;

// UC21 – Lấy thông tin profile user hiện tại
public interface GetUserProfileUseCase {
    UserProfileResult getUserProfile(String userEmail);
}
