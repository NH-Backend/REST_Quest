package io.nh_backend.rest_quest.user.dto;


import io.nh_backend.rest_quest.user.domain.Role;
import lombok.Builder;

@Builder
public record AccessTokenBody(
        String email,
        Role role
) {
}
