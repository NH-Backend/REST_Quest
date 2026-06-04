package io.nh_backend.rest_quest.npc.dto;

import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;

public record NpcShopItemResponse(
        Long id,
        Long rid,
        String itemName,
        ItemType itemType,
        ItemGrade itemGrade,
        String description,
        Integer goldPrice,
        Integer gemPrice,
        Integer sellPrice
) {

}
