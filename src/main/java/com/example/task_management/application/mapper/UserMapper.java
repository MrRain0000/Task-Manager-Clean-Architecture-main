package com.example.task_management.application.mapper;

import com.example.task_management.application.DTOUsecase.response.auth.AuthResult;
import com.example.task_management.application.DTOUsecase.response.auth.RegisterResult;
import com.example.task_management.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegisterResult toRegisterResult(User user) {
        return RegisterResult.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public AuthResult toAuthResult(User user, String accessToken) {
        return AuthResult.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .verified(user.isVerified())
                .build();
    }
}
