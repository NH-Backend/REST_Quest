package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;

import java.time.LocalDateTime;

public record UserItemResponse(
        Long userItemId,
        Long itemId,
        String rId,
        String itemName,
        String itemType,
        String itemGrade,
        String description,
        Integer goldPrice,
        Integer gemPrice,
        Integer sellPrice,
        Integer quantity,
        Boolean equipped,
        String acquiredAt
) {

}
