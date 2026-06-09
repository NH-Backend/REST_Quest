package io.nh_backend.rest_quest.common.dto;

public record KeyPair(
        String accessToken,
        String refreshToken
) {
}
