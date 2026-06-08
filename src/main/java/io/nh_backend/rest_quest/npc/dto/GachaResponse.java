package io.nh_backend.rest_quest.npc.dto;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.user.domain.Wallet;

public record GachaResponse(
        Wallet wallet,
        Item item,
        ItemType itemType,
        ItemGrade itemGrade,
        Integer gemCoupon,
        Integer goldCoupon,
        Integer expCoupon
) {
}
