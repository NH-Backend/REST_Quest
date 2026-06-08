package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import lombok.Builder;
import lombok.Getter;

@Builder
public record ItemResponse(
        Long itemId,
        String rId,
        String itemName,
        String itemType,
        String itemGrade,
        String description,
        Integer price,
        Integer gemPrice,
        Integer sellPrice
) {
    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .itemId(item.getId())
                .rId(item.getRId())
                .itemName(item.getItemName())
                .itemType(item.getItemType().name())
                .itemGrade(item.getItemGrade().name())
                .description(item.getDescription())
                .price(item.getGoldPrice())
                .gemPrice(item.getGemPrice())
                .sellPrice(item.getSellPrice())
                .build();
    }
}
