package io.nh_backend.rest_quest.npc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "npcs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Npc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String locationKey="village_entrance";

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "npc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NpcItem> shopItems = new ArrayList<>();

    @Builder
    public Npc(String rId, String name, String description, String locationKey, Boolean active) {
        this.rId = rId;
        this.name = name;
        this.description = description;
        if (locationKey != null) this.locationKey = locationKey;
        if (active != null) this.active = active;
    }

    public void addShopItem(NpcItem npcItem) {
        this.shopItems.add(npcItem);
        if (npcItem.getNpc() != this) {
            npcItem.assignNpc(this);
        }
    }
}