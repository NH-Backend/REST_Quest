package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.UserItem;

import java.time.LocalDateTime;

public record UserItemResponse(
        Long userItemId,
        Long itemId,
        String rId,
        String itemName,
        String itemType,
        String itemGrade,
        String description,
        Integer price,
        Integer sellPrice,
        Integer quantity,
        Boolean equipped,
        LocalDateTime acquiredAt

) {
    public static UserItemResponse from(UserItem userItem) {
        Item item = userItem.getItem();

        return new UserItemResponse(
                userItem.getId(),
                item.getId(),
                item.getRId(),
                item.getItemName(),
                item.getItemType().name(),
                item.getItemGrade().name(),
                item.getDescription(),
                item.getGoldPrice(),
                item.getSellPrice(),
                userItem.getQuantity(),
                userItem.getEquipped(),
                userItem.getAcquiredAt()
        );
    }
}
