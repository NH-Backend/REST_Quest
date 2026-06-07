package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.dto.NpcResponse;
import io.nh_backend.rest_quest.npc.repository.NpcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NpcService {
    private final NpcRepository npcRepository;

    /**
     * INF_UNITY_025: 활성화된 전체 NPC 목록 및 상점 아이템 카탈로그 조회
     */
    public List<NpcResponse> getAllActiveNpcs() {
        List<Npc> npcs = npcRepository.findAllActiveNpcsWithShopItems();
        return npcs.stream()
                .map(NpcResponse::from)
                .toList();
    }

    /**
     * INF_UNITY_026: 특정 NPC 단건 상세 정보 및 상점 아이템 카탈로그 조회
     */
    public NpcResponse getNpcDetails(Long id) {
        Npc npc = npcRepository.findNpcWithShopItemsById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NPC_NOT_FOUND));

        if (!npc.getActive()) {
            throw new BusinessException(ErrorCode.NPC_NOT_FOUND);
        }

        return NpcResponse.from(npc);
    }
}
