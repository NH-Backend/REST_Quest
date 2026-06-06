package io.nh_backend.rest_quest.item.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
import io.nh_backend.rest_quest.item.dto.ItemResponse;
import io.nh_backend.rest_quest.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ApiResponse<List<ItemResponse>> getAllItems() {
        return ApiResponse.ok(
                itemService.getAllItems(),
                SuccessCode.ITEM_READ.getSuccessMessage()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemResponse> getItemById(@PathVariable("id") Long id) {
        return ApiResponse.ok(
                itemService.getItemById(id), SuccessCode.ITEM_READ_SINGLE.getSuccessMessage()
        );
    }
}
