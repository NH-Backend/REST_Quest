package io.nh_backend.rest_quest.npc.dto;

import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;

import java.util.Comparator;
import java.util.List;

public record NpcResponse(
        Long npcId,         // 명세서 규격 필드명인 npcId 매칭 (Npc 엔티티의 id)
        String rId,         // NPC 리소스 ID (예: npc_merchant_001)
        String name,
        String description,
        String locationKey,
        Boolean active,
        List<NpcShopItemResponse> shopItems // 상점에서 파는 물건 리스트
) {
    public static NpcResponse from(Npc npc) {
        List<NpcShopItemResponse> items = npc.getShopItems().stream()
                .sorted(Comparator.comparing(NpcItem::getSortOrder))
                .map(NpcShopItemResponse::from)
                .toList();

        return new NpcResponse(
                npc.getId(),
                npc.getRId(),
                npc.getName(),
                npc.getDescription(),
                npc.getLocationKey(),
                npc.getActive(),
                items
        );
    }
}
