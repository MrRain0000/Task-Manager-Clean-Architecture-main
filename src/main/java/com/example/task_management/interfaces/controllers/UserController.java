package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.request.user.ChangePasswordCommand;
import com.example.task_management.application.DTOUsecase.request.user.UpdateUserProfileCommand;
import com.example.task_management.application.DTOUsecase.response.user.UserProfileResult;
import com.example.task_management.application.usecases.user.ChangePasswordUseCase;
import com.example.task_management.application.usecases.user.GetUserProfileUseCase;
import com.example.task_management.application.usecases.user.UpdateUserProfileUseCase;
import com.example.task_management.interfaces.dto.request.user.ChangePasswordRequest;
import com.example.task_management.interfaces.dto.request.user.UpdateUserProfileRequest;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.user.UserProfileResponse;
import com.example.task_management.interfaces.mappers.UserResponseMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UserResponseMapper userResponseMapper;

    public UserController(
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            UserResponseMapper userResponseMapper) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.userResponseMapper = userResponseMapper;
    }

    // API: Lấy thông tin profile user hiện tại
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String currentUserEmail = authentication.getName();

        UserProfileResult result = getUserProfileUseCase.getUserProfile(currentUserEmail);
        UserProfileResponse responseData = userResponseMapper.toUserProfileResponse(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "Lấy thông tin profile thành công", responseData));
    }

    // API: Cập nhật thông tin profile user
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();

        UserProfileResult result = updateUserProfileUseCase.updateUserProfile(
                currentUserEmail,
                UpdateUserProfileCommand.builder()
                        .username(request.getUsername())
                        .build());

        UserProfileResponse responseData = userResponseMapper.toUserProfileResponse(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "Cập nhật profile thành công", responseData));
    }

    // API: Đổi mật khẩu
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();

        changePasswordUseCase.changePassword(
                currentUserEmail,
                ChangePasswordCommand.builder()
                        .currentPassword(request.getCurrentPassword())
                        .newPassword(request.getNewPassword())
                        .build());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "Đổi mật khẩu thành công", null));
    }
}
