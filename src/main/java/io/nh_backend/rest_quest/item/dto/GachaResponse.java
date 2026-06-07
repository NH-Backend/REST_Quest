package io.nh_backend.rest_quest.item.dto;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;

public record GachaResponse(
        WalletResponse wallet,
        ProfileResponse profile,
        DrawnItemResponse drawnItem,
        RewardResponse reward,
        UserItemResponse acquiredInventoryItem
) {

    public static GachaResponse of(
            Wallet wallet,
            UserProfile profile,
            Item drawnItem,
            RewardResponse reward,
            UserItem acquiredInventoryItem
    ) {
        return new GachaResponse(
                WalletResponse.from(wallet),
                ProfileResponse.from(profile),
                DrawnItemResponse.from(drawnItem),
                reward,
                acquiredInventoryItem == null ? null : UserItemResponse.from(acquiredInventoryItem)
        );
    }

    public record WalletResponse(
            Integer gold,
            Integer gem
    ) {
        public static WalletResponse from(Wallet wallet) {
            return new WalletResponse(wallet.getGold(), wallet.getGem());
        }
    }

    public record ProfileResponse(
            Long exp
    ) {
        public static ProfileResponse from(UserProfile profile) {
            return new ProfileResponse(profile.getExp());
        }
    }

    public record DrawnItemResponse(
            Long itemId,
            String rId,
            String itemName,
            String itemType,
            String itemGrade,
            Integer goldCoupon,
            Integer gemCoupon,
            Integer expCoupon
    ) {
        public static DrawnItemResponse from(Item item) {
            return new DrawnItemResponse(
                    item.getId(),
                    item.getRId(),
                    item.getItemName(),
                    item.getItemType().name(),
                    item.getItemGrade().name(),
                    item.getGoldCoupon(),
                    item.getGemCoupon(),
                    item.getExpCoupon()
            );
        }
    }

    public record RewardResponse(
            String type,
            Integer amount
    ) {
    }
}
