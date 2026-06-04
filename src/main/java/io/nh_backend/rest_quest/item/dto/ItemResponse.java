package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;

public record ItemResponse(
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
