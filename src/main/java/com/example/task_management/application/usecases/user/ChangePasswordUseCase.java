package com.example.task_management.application.usecases.user;

import com.example.task_management.application.DTOUsecase.request.user.ChangePasswordCommand;

// UC23 – Đổi mật khẩu user
public interface ChangePasswordUseCase {
    void changePassword(String userEmail, ChangePasswordCommand command);
}
