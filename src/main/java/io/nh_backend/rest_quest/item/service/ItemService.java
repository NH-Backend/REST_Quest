package io.nh_backend.rest_quest.item.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.dto.ItemResponse;
import io.nh_backend.rest_quest.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    // 전체 아이템 조회
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    // 아이템 단건 조회
    public ItemResponse getItemById (Long id){
        Item item = itemRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        return ItemResponse.from(item);
    }
}
