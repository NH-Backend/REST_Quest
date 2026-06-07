package io.nh_backend.rest_quest.friend_request.dto;

import io.nh_backend.rest_quest.friend_request.domain.FriendRequest;
import io.nh_backend.rest_quest.user.domain.User;

import java.time.format.DateTimeFormatter;

public record FriendResponse(
        Long friendRequestId,
        Long fromUserId,
        Long toUserId,
        String status,
        String createdAt,
        String nickname
) {
    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static FriendResponse from(FriendRequest friendRequest, User counterpart) {
        return new FriendResponse(
                friendRequest.getId(),
                friendRequest.getFromUser().getId(),
                friendRequest.getToUser().getId(),
                friendRequest.getStatus().name(),
                friendRequest.getCreatedAt().format(CREATED_AT_FORMATTER),
                counterpart.getNickname()
        );
    }
}
