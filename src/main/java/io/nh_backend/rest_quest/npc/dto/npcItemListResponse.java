package io.nh_backend.rest_quest.npc.dto;

import java.util.List;

public record npcItemListResponse(

        Long npcId,
        String npcRId,
        String name,
        String npcDescription,
        String locationKey,
        Boolean active,
        List<NpcShopItemResponse> shopItems

) {

}
