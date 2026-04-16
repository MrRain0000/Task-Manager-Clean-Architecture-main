package com.example.task_management.application.DTOUsecase.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command để đổi mật khẩu user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordCommand {
    private String currentPassword;
    private String newPassword;
}
