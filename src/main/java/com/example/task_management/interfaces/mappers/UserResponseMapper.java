package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.user.UserProfileResult;
import com.example.task_management.interfaces.dto.response.user.UserProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper {

    public UserProfileResponse toUserProfileResponse(UserProfileResult result) {
        if (result == null) {
            return null;
        }
        return UserProfileResponse.builder()
                .id(result.getId())
                .username(result.getUsername())
                .email(result.getEmail())
                .isVerified(result.isVerified())
                .totalProjects(result.getTotalProjects())
                .totalTasks(result.getTotalTasks())
                .build();
    }
}
