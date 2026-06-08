package io.nh_backend.rest_quest.npc.dto;

import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.npc.domain.NpcItem;

public record NpcShopItemResponse(
        Long npcItemId,     // NpcItem의 고유 PK (명세서 필수 항목)
        Long itemId,        // 마스터 Item ID
        String rId,         // 아이템 리소스 ID (예: sword_001)
        String itemName,
        String itemType,    // WEAPON, CONSUMABLE, GEM_COUPON 등
        String itemGrade,   // COMMON, EPIC 등
        String description,
        Integer gemPrice,   // 명세서 026번 기준 가격 2종 수용
        Integer goldPrice,
        Integer sellPrice,
        Integer quantity,   // 상점 내 남은 재고 수량
        Integer sortOrder   // 정렬 순서
) {
    public static NpcShopItemResponse from(NpcItem npcItem) {
        return new NpcShopItemResponse(
                npcItem.getId(),
                npcItem.getItem().getId(),
                npcItem.getItem().getRId(),
                npcItem.getItem().getItemName(),
                npcItem.getItem().getItemType().name(),
                npcItem.getItem().getItemGrade().name(),
                npcItem.getItem().getDescription(),
                npcItem.getItem().getGemPrice(),
                npcItem.getItem().getGoldPrice(),
                npcItem.getItem().getSellPrice(),
                npcItem.getQuantity(),
                npcItem.getSortOrder()
        );
    }
}
