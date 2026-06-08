package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.npc.dto.GachaResponse;
import io.nh_backend.rest_quest.item.repository.ItemRepository;
import io.nh_backend.rest_quest.item.repository.UserItemRepository;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import io.nh_backend.rest_quest.npc.repository.NpcItemRepository;
import io.nh_backend.rest_quest.npc.repository.NpcRepository;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.user.repository.UserProfileRepository;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import io.nh_backend.rest_quest.user.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NpcGachaService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserProfileRepository userProfileRepository;
    private final NpcRepository npcRepository;
    private final NpcItemRepository npcItemRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;

    @Transactional
    public GachaResponse purchaseAndDraw(String email, Long npcId, Long npcItemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        Npc npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NPC_NOT_FOUND));
        NpcItem npcItem = npcItemRepository.findByIdAndNpc(npcItemId, npc)
                .orElseThrow(() -> new BusinessException(ErrorCode.NPC_ITEM_NOT_FOUND));

        Item gachaItem = npcItem.getItem();


        if (gachaItem.getItemType() != ItemType.GACHA && gachaItem.getItemType() != ItemType.GOLD_EXCHANGE) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        wallet.pay(gachaItem.getGoldPrice(), gachaItem.getGemPrice());

        npcItem.decreaseStock(1);

        Item drawnItem;
        RewardResult rewardResult;

        if (gachaItem.getItemType() == ItemType.GOLD_EXCHANGE) {
            wallet.addGold(gachaItem.getGoldCoupon());
            drawnItem = gachaItem; // 환전 상품 자체를 타겟으로 지정
            rewardResult = new RewardResult("GOLD", gachaItem.getGoldCoupon(), null);
        }

        else {
            drawnItem = drawRewardItemByProbability(gachaItem.getRId(), gachaItem.getId());
            rewardResult = applyReward(user, wallet, profile, drawnItem);
        }

        return GachaResponse.of(
                wallet,
                profile,
                drawnItem,
                new GachaResponse.RewardResponse(rewardResult.type(), rewardResult.amount()),
                rewardResult.acquiredInventoryItem()
        );
    }

    private ItemGrade calculateDrawnGrade(String rId) {
        int dice = ThreadLocalRandom.current().nextInt(100000); // 0 ~ 99999 주사위

        switch (rId) {
            case "gacha_bronze":
                // COMMON 80% (80000) | UNCOMMON 15% (15000) | RARE 5% (5000)
                if (dice < 80000) return ItemGrade.COMMON;
                if (dice < 95000) return ItemGrade.UNCOMMON;
                return ItemGrade.RARE;

            case "gacha_silver":
                // COMMON 35% (35000) | UNCOMMON 57% (57000) | RARE 8% (8000)
                if (dice < 35000) return ItemGrade.COMMON;
                if (dice < 92000) return ItemGrade.UNCOMMON;
                return ItemGrade.RARE;

            case "gacha_gold":
                // COMMON 5% (5000) | UNCOMMON 25% (25000) | RARE 65% (65000) | EPIC 5% (5000)
                if (dice < 5000) return ItemGrade.COMMON;
                if (dice < 30000) return ItemGrade.UNCOMMON;
                if (dice < 95000) return ItemGrade.RARE;
                return ItemGrade.EPIC;

            case "gacha_master":
                // COMMON 1% (1000) | UNCOMMON 19% (19000) | RARE 63% (63000) | EPIC 16.5% (16500) | LEGENDARY 0.5% (500)
                if (dice < 1000) return ItemGrade.COMMON;
                if (dice < 20000) return ItemGrade.UNCOMMON;
                if (dice < 83000) return ItemGrade.RARE;
                if (dice < 99500) return ItemGrade.EPIC;
                return ItemGrade.LEGENDARY;

            case "gacha_challenger":
                // COMMON 0.1% (100) | UNCOMMON 1.9% (1900) | RARE 20% (20000) | EPIC 53% (53000) | LEGENDARY 25% (25000)
                if (dice < 100) return ItemGrade.COMMON;
                if (dice < 2000) return ItemGrade.UNCOMMON;
                if (dice < 22000) return ItemGrade.RARE;
                if (dice < 75000) return ItemGrade.EPIC;
                return ItemGrade.LEGENDARY;

            default:
                throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
    }


    private Item drawRewardItemByProbability(String boxRId, Long purchasedItemId) {

        ItemGrade targetGrade = calculateDrawnGrade(boxRId);

        List<Item> candidates = itemRepository.findAllByItemGrade(targetGrade).stream()
                .filter(item -> !Objects.equals(item.getId(), purchasedItemId))
                .filter(item -> item.getItemType() != ItemType.GACHA)
                .filter(item -> item.getItemType() != ItemType.GOLD_EXCHANGE)
                .toList();

        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }

        int index = ThreadLocalRandom.current().nextInt(candidates.size());
        return candidates.get(index);
    }

    private RewardResult applyReward(User user, Wallet wallet, UserProfile profile, Item drawnItem) {
        return switch (drawnItem.getItemType()) {
            case GOLD_COUPON -> {
                wallet.addGold(drawnItem.getGoldCoupon());
                yield new RewardResult("GOLD", drawnItem.getGoldCoupon(), null);
            }
            case GEM_COUPON -> {
                wallet.addGem(drawnItem.getGemCoupon());
                yield new RewardResult("GEM", drawnItem.getGemCoupon(), null);
            }
            case EXP_COUPON -> {
                profile.addExp(drawnItem.getExpCoupon());
                yield new RewardResult("EXP", drawnItem.getExpCoupon(), null);
            }
            default -> {
                UserItem acquiredInventoryItem = addItemToInventory(user, drawnItem);
                yield new RewardResult("ITEM", 1, acquiredInventoryItem);
            }
        };
    }

    private UserItem addItemToInventory(User user, Item item) {
        return userItemRepository.findByUserAndItemAndDeletedAtIsNull(user, item)
                .map(existingItem -> {
                    existingItem.addQuantity(1);
                    return existingItem;
                })
                .orElseGet(() -> userItemRepository.save(
                        UserItem.builder()
                                .user(user)
                                .item(item)
                                .quantity(1)
                                .equipped(false)
                                .build()
                ));
    }

    private record RewardResult(
            String type,
            Integer amount,
            UserItem acquiredInventoryItem
    ) {
    }
}
