package io.nh_backend.rest_quest.item.domain;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Integer quantity;

    private Boolean equipped = false;

    private LocalDateTime acquiredAt;

    private LocalDateTime deletedAt;

    @Builder
    public UserItem(User user, Item item, Integer quantity, Boolean equipped) {
        this.user = user;
        this.item = item;
        this.quantity = quantity;
        this.equipped = equipped == null ? false : equipped;
        this.acquiredAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.QUANTITY_UNDER_ONE);
        }
        this.quantity += amount;
        this.acquiredAt = LocalDateTime.now();
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.QUANTITY_UNDER_ONE);
        }
        if (this.quantity < amount) {
            throw new BusinessException(ErrorCode.ITEM_STOCK_SHORTAGE);
        }
        this.quantity -= amount;
    }
}