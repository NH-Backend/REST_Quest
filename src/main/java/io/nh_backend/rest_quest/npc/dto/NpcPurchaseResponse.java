package io.nh_backend.rest_quest.npc.dto;

import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.user.domain.Wallet;

public record NpcPurchaseResponse(
        WalletDto wallet,
        UserItemResponse acquiredItem
) {
    public record WalletDto(
            Long gold,
            Long gem
    ) {}

    public static NpcPurchaseResponse of(Wallet wallet, UserItemResponse acquiredItem) {
        return new NpcPurchaseResponse(
                new WalletDto(wallet.getGold().longValue(), wallet.getGem().longValue()),
                acquiredItem
        );
    }
}
