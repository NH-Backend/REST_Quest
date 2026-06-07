package io.nh_backend.rest_quest.item.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
import io.nh_backend.rest_quest.item.dto.UserItemQuantityRequest;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.item.service.UserItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/users/me/inventory")
public class InventoryController {

    private final UserItemService userItemService;

    /**
     * 내 인벤토리 전체 목록 조회
     * GET /api/v1/users/me/inventory
     */
    @GetMapping
    public ApiResponse<List<UserItemResponse>> getMyInventory(Principal principal) {

        return ApiResponse.ok(
                userItemService.getMyInventory(principal.getName()),
                SuccessCode.INVENTORY_READ.getSuccessMessage()
        );
    }

    /**
     * 아이템 획득
     * POST /api/v1/users/me/inventory/pickup
     */
    @PostMapping("/pickup")
    public ApiResponse<UserItemResponse> addItemToInventory(
            Principal principal,
            @Valid @RequestBody UserItemQuantityRequest request
    ) {
        return ApiResponse.ok(
                userItemService.addItemToInventory(principal.getName(), request),
                SuccessCode.ITEM_GET.getSuccessMessage()
        );
    }

    /**
     * 아이템 버리기
     * DELETE /api/v1/users/me/inventory/{userItemId}/discard?quantity=?
     */
    @DeleteMapping("/{userItemId}/discard")
    public ApiResponse<Void> discardItem(
            Principal principal,
            @PathVariable("userItemId") @Positive(message = "인벤토리 식별자는 양수여야 합니다.") Long userItemId,
            @RequestParam("quantity") @Min(value = 1, message = "요청 수량은 1개 이상이어야 합니다.") Integer quantity
    ) {
        UserItemQuantityRequest request = new UserItemQuantityRequest(userItemId, quantity);

        userItemService.discardItem(principal.getName(), request);
        return ApiResponse.ok(null, SuccessCode.ITEM_DISCARD.getSuccessMessage());
    }
}
