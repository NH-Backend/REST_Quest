package io.nh_backend.rest_quest.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer gold = 3000;

    private Integer gem = 100;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Wallet(Integer gold, Integer gem, User user) {
        this.gold = gold == null ? 3000 : gold;
        this.gem = gem == null ? 100 : gem;
        this.user = user;
    }
}