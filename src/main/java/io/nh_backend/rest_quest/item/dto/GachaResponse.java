package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.user.domain.Wallet;
import jakarta.persistence.Column;

public record GachaResponse(
        Wallet wallet,
        Object item,
        ItemType itemType,
        ItemGrade itemGrade,
        Integer gemCoupon,
        Integer goldCoupon,
        Integer expCoupon
) {
}
