package io.nh_backend.rest_quest.user.dto;

import io.nh_backend.rest_quest.item.dto.UserItemResponse;

import java.util.List;

public record UserDataResponse(
        UserResponse account,
        UserProfileResponse profile,
        ShowWalletResponse wallet,
        List<UserItemResponse> inventory,
        Long friendCount
) {
}
