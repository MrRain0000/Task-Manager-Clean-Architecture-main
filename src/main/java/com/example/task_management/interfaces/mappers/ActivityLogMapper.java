package com.example.task_management.interfaces.mappers;

import com.example.task_management.application.DTOUsecase.response.activitylog.ActivityLogResult;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.interfaces.dto.response.activitylog.ActivityLogResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi ActivityLog từ Application DTO sang Interface DTO.
 * Tầng Interface chỉ được phép nhìn thấy Application DTO, không được nhìn Domain Entity.
 */
@Component
public class ActivityLogMapper {

    private final UserRepository userRepository;

    public ActivityLogMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Map ActivityLogResult (Application) → ActivityLogResponse (Interface)
     * Include thông tin user từ UserRepository.
     */
    public ActivityLogResponse toResponse(ActivityLogResult log) {
        if (log == null) {
            return null;
        }

        // Lấy user info từ repository
        ActivityLogResponse.UserInfo userInfo = userRepository.findById(log.getUserId())
            .map(user -> ActivityLogResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build())
            .orElseGet(() -> ActivityLogResponse.UserInfo.builder()
                .id(log.getUserId())
                .username("Unknown")
                .build());

        return ActivityLogResponse.builder()
            .id(log.getId())
            .user(userInfo)
            .actionType(log.getActionType())
            .entityType(log.getEntityType())
            .entityId(log.getEntityId())
            .description(log.getDescription())
            .metadata(log.getMetadata())
            .createdAt(log.getCreatedAt())
            .build();
    }
}
