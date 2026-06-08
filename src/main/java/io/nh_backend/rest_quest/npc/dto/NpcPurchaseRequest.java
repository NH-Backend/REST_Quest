package io.nh_backend.rest_quest.npc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NpcPurchaseRequest(
        @NotNull(message = "구매 수량은 필수입니다.")
        @Positive(message = "구매 수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
