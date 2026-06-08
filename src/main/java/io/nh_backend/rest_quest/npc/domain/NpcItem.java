package io.nh_backend.rest_quest.npc.domain;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "npc_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NpcItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = false)
    private Npc npc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer sortOrder;

    @Builder
    public NpcItem(Npc npc, Item item, Integer quantity, Integer sortOrder) {
        this.npc = npc;
        this.item = item;
        this.quantity = quantity;
        this.sortOrder = sortOrder;
    }

    protected void assignNpc(Npc npc) {
        this.npc = npc;
    }

    public void decreaseStock(int amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.QUANTITY_UNDER_ONE);
        }
        if (this.quantity < amount) {
            throw new BusinessException(ErrorCode.SHOP_STOCK_SHORTAGE);
        }
        this.quantity -= amount;
    }
}