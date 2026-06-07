package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.item.dto.GachaResponse;
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
        if (gachaItem.getItemType() != ItemType.GACHA) {
            throw new BusinessException(ErrorCode.INVALID_GACHA_ITEM);
        }

        wallet.pay(gachaItem.getGoldPrice(), gachaItem.getGemPrice());

        Item drawnItem = drawRewardItem(gachaItem.getItemGrade(), gachaItem.getId());
        RewardResult rewardResult = applyReward(user, wallet, profile, drawnItem);

        return GachaResponse.of(
                wallet,
                profile,
                drawnItem,
                new GachaResponse.RewardResponse(rewardResult.type(), rewardResult.amount()),
                rewardResult.acquiredInventoryItem()
        );
    }

    private Item drawRewardItem(ItemGrade itemGrade, Long purchasedItemId) {
        List<Item> candidates = itemRepository.findAllByItemGrade(itemGrade).stream()
                .filter(item -> !Objects.equals(item.getId(), purchasedItemId))
                .filter(item -> item.getItemType() != ItemType.GACHA)
                .filter(item -> item.getItemType() != ItemType.GOLD_EXCHANGE)
                .toList();

        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.GACHA_REWARD_NOT_FOUND);
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
