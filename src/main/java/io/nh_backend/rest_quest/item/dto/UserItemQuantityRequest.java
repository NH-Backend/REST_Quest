package io.nh_backend.rest_quest.item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserItemQuantityRequest(
        @NotNull(message = "식별자는 필수입니다.")
        @Positive(message = "식별자는 양수여야 합니다.")
        Long itemId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "요청 수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
