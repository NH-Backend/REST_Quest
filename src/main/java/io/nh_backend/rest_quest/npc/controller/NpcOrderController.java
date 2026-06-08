package io.nh_backend.rest_quest.npc.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
import io.nh_backend.rest_quest.npc.dto.NpcPurchaseRequest;
import io.nh_backend.rest_quest.npc.dto.NpcPurchaseResponse;
import io.nh_backend.rest_quest.npc.service.NpcOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/npcs")
public class NpcOrderController {
    private final NpcOrderService npcOrderService;

    /**
     * INF_UNITY_027: NPC 상점 아이템 구매 API (POST)
     */
    @PostMapping("/{npcId}/items/{npcItemId}/purchase")
    public ApiResponse<NpcPurchaseResponse> purchaseItem(
            Principal principal,
            @PathVariable("npcId") @Positive(message = "NPC 식별자는 양수여야 합니다.") Long npcId,
            @PathVariable("npcItemId") @Positive(message = "상점 상품 식별자는 양수여야 합니다.") Long npcItemId,
            @RequestBody @Valid NpcPurchaseRequest request
    ) {
        // 서비스가 완벽하게 포장까지 끝내온 응답 DTO를 다이렉트로 받아서 반환합니다.
        NpcPurchaseResponse response = npcOrderService.purchaseItem(principal.getName(), npcId, npcItemId, request.quantity());

        return ApiResponse.ok(
                response,
                SuccessCode.NPC_ITEM_PURCHASE_SUCCESS.getSuccessMessage()
        );
    }
}
