package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.dto.UserItemQuantityRequest;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.item.service.UserItemService;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import io.nh_backend.rest_quest.npc.dto.NpcPurchaseResponse;
import io.nh_backend.rest_quest.npc.repository.NpcItemRepository;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import io.nh_backend.rest_quest.user.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NpcOrderService {
    private final NpcItemRepository npcItemRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserItemService userItemService;

    /**
     * NPC 상점 아이템 구매 통합 비즈니스 (실용주의적 Thin Controller - Thick Service 구조)
     */
    @Transactional
    public NpcPurchaseResponse purchaseItem(String email, Long npcId, Long npcItemId, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));

        // 우회 구매 위조 핵 방어 조건절 가동
        // TODO: 향후 매대 동시성 이슈 발생 시 NpcItemRepository에 비관적 락(PESSIMISTIC_WRITE) 검토
        NpcItem npcItem = npcItemRepository.findPurchaseTarget(npcId, npcItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NPC_NOT_FOUND));

        Npc npc = npcItem.getNpc();
        if (!npc.getActive()) {
            throw new BusinessException(ErrorCode.NPC_NOT_FOUND);
        }

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        Item item = npcItem.getItem();

        // [시니어 가이드 정렬 순서 준수]: 결제 재화 검증 및 즉각 차감 처리 (돈 없는 유저 선 탈락)
        if (item.getGoldPrice() != null && item.getGoldPrice() > 0) {
            int totalGoldPrice = item.getGoldPrice() * quantity;
            wallet.consumeGold(totalGoldPrice);
        }

        if (item.getGemPrice() != null && item.getGemPrice() > 0) {
            int totalGemPrice = item.getGemPrice() * quantity;
            wallet.consumeGem(totalGemPrice);
        }

        // 결제 통과 완료 시 상점 매대 재고 최종 감소
        npcItem.decreaseStock(quantity);

        // 내 가방 인벤토리에 실시간 누적 지급 처리 위임
        UserItemQuantityRequest inventoryRequest = new UserItemQuantityRequest(item.getId(), quantity);
        UserItemResponse acquiredItem = userItemService.addItemToInventory(email, inventoryRequest);

        // 🌟 [최종 교정 완수] 중간 Result 객체를 거치지 않고 최종 Response DTO를 직접 조립하여 반환!
        // 이로써 클래스 낭비를 막고 컨트롤러의 부담을 완벽하게 소멸시킵니다.
        return NpcPurchaseResponse.of(wallet, acquiredItem);
    }
}
