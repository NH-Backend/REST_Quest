package io.nh_backend.rest_quest.item.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.item.dto.UserItemQuantityRequest;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.item.repository.ItemRepository;
import io.nh_backend.rest_quest.item.repository.UserItemRepository;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserItemService {
    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    /**
     * 내 인벤토리 전체 목록 조회
     */
    public List<UserItemResponse> getMyInventory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));

        return userItemRepository.findAllByUserAndDeletedAtIsNull(user).stream()
                .map(this::toUserItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * 아이템 지급
     * 가방에 이미 존재하면 수량(quantity)을 누적 더하고, 없으면 새로 지급합니다.
     */
    @Transactional
    public UserItemResponse addItemToInventory(String email, UserItemQuantityRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));

        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        // 가방에 이미 동일 아이템이 존재하는지 확인
        UserItem userItem = userItemRepository.findByUserAndItemAndDeletedAtIsNull(user, item)
                .map(existingItem -> {
                    existingItem.addQuantity(request.quantity());
                    return existingItem;
                })
                .orElseGet(() -> {
                    // 가방에 처음 들어오는 아이템이라면 신규 생성
                    return userItemRepository.save(
                            UserItem.builder()
                                    .user(user)
                                    .item(item)
                                    .quantity(request.quantity())
                                    .equipped(false)
                                    .build()
                    );
                });

        return toUserItemResponse(userItem);
    }

    /**
     * 🗑️ 아이템 버리기
     * 버리려는 수량이 가방 속 수량과 똑같으면 Soft Delete를 수행하고, 남으면 수량만 차감합니다.
     */
    @Transactional
    public void discardItem(String email, UserItemQuantityRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_USER));

        UserItem userItem = userItemRepository.findByIdAndUserAndDeletedAtIsNull(request.itemId(), user)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND)); // 인벤토리에 해당 장비 없음

        if (userItem.getQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.ITEM_STOCK_SHORTAGE);
        }

        if (userItem.getQuantity().equals(request.quantity())) {
            // 가방 수량과 버릴 수량이 딱 맞아떨어지면 soft delete 수행
            userItem.delete();
        } else {
            userItem.decreaseQuantity(request.quantity());
        }
    }

    private UserItemResponse toUserItemResponse(UserItem userItem) {
       DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String formattedAcquiredAt = userItem.getAcquiredAt() != null
                ? userItem.getAcquiredAt().format(formatter)
                : "";

        return new UserItemResponse(
                userItem.getId(),                           // userItemId
                userItem.getItem().getId(),                 // itemId
                userItem.getItem().getRId(),                // rId
                userItem.getItem().getItemName(),           // itemName
                userItem.getItem().getItemType().name(),    // itemType
                userItem.getItem().getItemGrade().name(),   // itemGrade
                userItem.getItem().getDescription(),        // description
                userItem.getItem().getGoldPrice(),          // goldPrice
                userItem.getItem().getGemPrice(),           // gemPrice
                userItem.getItem().getSellPrice(),          // sellPrice
                userItem.getQuantity(),                     // quantity
                userItem.getEquipped(),                     // equipped
                formattedAcquiredAt                         // acquiredAt
        );
    }

}
