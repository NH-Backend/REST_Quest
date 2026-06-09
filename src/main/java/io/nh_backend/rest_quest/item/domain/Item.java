package io.nh_backend.rest_quest.item.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String rId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer goldPrice;

    @Column(nullable = false)
    private Integer gemPrice;

    @Column(nullable = false)
    private Integer sellPrice;

    @Column(nullable = false)
    private Integer expCoupon=0;

    @Column(nullable = false)
    private Integer gemCoupon=0;

    @Column(nullable = false)
    private Integer goldCoupon=0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemGrade itemGrade;

    @Builder
    public Item(String itemName, String rId, String description,
                Integer goldPrice, Integer gemPrice,Integer sellPrice,
                Integer expCoupon, Integer gemCoupon, Integer goldCoupon,
                ItemType itemType, ItemGrade itemGrade) {
        this.itemName = itemName;
        this.rId = rId;
        this.description = description;
        this.goldPrice = goldPrice;
        this.gemPrice = gemPrice;
        this.sellPrice = sellPrice;
        this.expCoupon = expCoupon;
        this.gemCoupon = gemCoupon;
        this.goldCoupon = goldCoupon;
        this.itemType = itemType;
        this.itemGrade = itemGrade;
    }
}