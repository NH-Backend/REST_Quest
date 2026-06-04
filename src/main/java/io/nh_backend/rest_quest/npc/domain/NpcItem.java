package io.nh_backend.rest_quest.npc.domain;

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
    @JoinColumn(name = "npcId", nullable = false)
    private Npc npc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId", nullable = false)
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
}