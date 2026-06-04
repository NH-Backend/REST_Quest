package io.nh_backend.rest_quest.user.dto;

import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;

import java.util.List;

public record UserDataResponse(
        UserResponse account,
        UserProfile profile,
        Integer level,
        Long exp,
        Long totalPlaySeconds,
        Wallet wallet,
        Long gold,
        Long gem,
        List<UserItemResponse> inventory,
        Long friendCount
) {
}
