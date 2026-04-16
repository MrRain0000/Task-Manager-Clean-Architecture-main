package com.example.task_management.application.usecases.user;

import com.example.task_management.application.DTOUsecase.request.user.UpdateUserProfileCommand;
import com.example.task_management.application.DTOUsecase.response.user.UserProfileResult;

// UC22 – Cập nhật thông tin profile user
public interface UpdateUserProfileUseCase {
    UserProfileResult updateUserProfile(String userEmail, UpdateUserProfileCommand command);
}
