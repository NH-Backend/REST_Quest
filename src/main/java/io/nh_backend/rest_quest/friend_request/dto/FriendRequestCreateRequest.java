package io.nh_backend.rest_quest.friend_request.dto;

import jakarta.validation.constraints.NotNull;

public record FriendRequestCreateRequest(
        @NotNull Long toUserId
) {
}
