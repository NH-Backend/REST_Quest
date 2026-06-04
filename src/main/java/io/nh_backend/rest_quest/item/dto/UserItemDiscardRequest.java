package io.nh_backend.rest_quest.item.dto;

public record UserItemDiscardRequest(
        Long id,
        Integer quantity
) {
}
