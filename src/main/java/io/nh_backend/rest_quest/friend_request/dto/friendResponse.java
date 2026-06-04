package io.nh_backend.rest_quest.friend_request.dto;

public record friendResponse(
        Long Id,
        Long fromUserId,
        Long toUserId,
        String status,
        String createdAt,
        String nickname
) {
}
