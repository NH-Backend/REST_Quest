package io.nh_backend.rest_quest.user.dto;

public record RefreshTokenRotation(
        String accessToken,
        String refreshToken,
        long accessExpiresInSeconds) {
}
