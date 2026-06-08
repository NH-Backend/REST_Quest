package io.nh_backend.rest_quest.npc.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
import io.nh_backend.rest_quest.npc.dto.NpcResponse;
import io.nh_backend.rest_quest.npc.service.NpcService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/npcs")
@RequiredArgsConstructor
@Validated
public class NpcController {
    private final NpcService npcService;

    /**
     * INF_UNITY_025: NPC 목록 조회 (인증 불필요)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NpcResponse>>> getAllActiveNpcs() {
        List<NpcResponse> response = npcService.getAllActiveNpcs();

        return ResponseEntity.ok(
                ApiResponse.ok(response, SuccessCode.NPC_LIST_READ.getSuccessMessage())
        );
    }

    /**
     * INF_UNITY_026: NPC 단건 조회 (인증 불필요)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NpcResponse>> getNpcDetails(@PathVariable("id") @Positive Long id) {
        NpcResponse response = npcService.getNpcDetails(id);

        return ResponseEntity.ok(
                ApiResponse.ok(response, SuccessCode.NPC_DETAIL_READ.getSuccessMessage())
        );
    }
}
