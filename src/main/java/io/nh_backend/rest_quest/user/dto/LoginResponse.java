package io.nh_backend.rest_quest.user.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresInSeconds) {
}
