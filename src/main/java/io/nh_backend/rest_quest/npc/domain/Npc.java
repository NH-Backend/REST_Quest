package io.nh_backend.rest_quest.npc.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "npc")
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
    private String locationKey;

    private Boolean active = false;

    @Builder
    public Npc(String rId, String name, String description, String locationKey, Boolean active) {
        this.rId = rId;
        this.name = name;
        this.description = description;
        this.locationKey = locationKey;
        this.active = active == null ? false : active;
    }
}