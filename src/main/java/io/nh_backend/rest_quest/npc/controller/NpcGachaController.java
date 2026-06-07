package io.nh_backend.rest_quest.npc.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
import io.nh_backend.rest_quest.item.dto.GachaResponse;
import io.nh_backend.rest_quest.npc.service.NpcGachaService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/users/me/npcs")
public class NpcGachaController {

    private final NpcGachaService npcGachaService;

    @PostMapping("/{npcId}/items/{npcItemId}/gacha")
    public ApiResponse<GachaResponse> purchaseAndDraw(
            Principal principal,
            @PathVariable("npcId") @Positive Long npcId,
            @PathVariable("npcItemId") @Positive Long npcItemId
    ) {
        return ApiResponse.ok(
                npcGachaService.purchaseAndDraw(principal.getName(), npcId, npcItemId),
                SuccessCode.GACHA_PURCHASED.getSuccessMessage()
        );
    }
}
