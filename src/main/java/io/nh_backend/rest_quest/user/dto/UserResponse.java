package io.nh_backend.rest_quest.user.dto;


import io.nh_backend.rest_quest.user.domain.Provider;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.Status;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Role  role,
        Status status,
        Provider provider,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
}
