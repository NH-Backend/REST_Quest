package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;

import java.time.LocalDateTime;

public record UserItemResponse(
        Long userItemId,
        Long itemId,
        Long rid,
        String itemName,
        ItemType itemType,
        ItemGrade itemGrade,
        String description,
        Integer goldPrice,
        Integer gemPrice,
        Integer sellPrice,
        Boolean equipped,
        LocalDateTime acquiredAt

) {

}
