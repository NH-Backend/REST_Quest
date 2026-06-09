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

    @Transactional
    public NpcPurchaseResponse purchaseItem(String email, Long npcId, Long npcItemId, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));

        NpcItem npcItem = npcItemRepository.findPurchaseTarget(npcId, npcItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NPC_NOT_FOUND));

        Npc npc = npcItem.getNpc();
        if (!npc.getActive()) {
            throw new BusinessException(ErrorCode.NPC_NOT_FOUND);
        }

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        Item item = npcItem.getItem();

        if (item.getGoldPrice() != null && item.getGoldPrice() > 0) {
            int totalGoldPrice = item.getGoldPrice() * quantity;
            wallet.consumeGold(totalGoldPrice);
        }

        if (item.getGemPrice() != null && item.getGemPrice() > 0) {
            int totalGemPrice = item.getGemPrice() * quantity;
            wallet.consumeGem(totalGemPrice);
        }

        npcItem.decreaseStock(quantity);

        UserItemQuantityRequest inventoryRequest = new UserItemQuantityRequest(item.getId(), quantity);
        UserItemResponse acquiredItem = userItemService.addItemToInventory(email, inventoryRequest);

        return NpcPurchaseResponse.of(wallet, acquiredItem);
    }
}
