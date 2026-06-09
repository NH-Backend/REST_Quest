package io.nh_backend.rest_quest.user.domain;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
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
     
    /**
     * 골드 재화 검증 및 차감 도메인 메서드
     */
    public void consumeGold(int amount) {
        if (this.gold < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_GOLD);
        }
        this.gold -= amount;
    }

    /**
     * 보석 재화 검증 및 차감 도메인 메서드 (뽑기/프리미엄 대비)
     */
    public void consumeGem(int amount) {
        if (this.gem < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_GEM);
        }
        this.gem -= amount;
    }

    public void pay(Integer goldPrice, Integer gemPrice) {
        int requiredGold = goldPrice == null ? 0 : goldPrice;
        int requiredGem = gemPrice == null ? 0 : gemPrice;

        if (this.gold < requiredGold || this.gem < requiredGem) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_CURRENCY);
        }

        this.gold -= requiredGold;
        this.gem -= requiredGem;
    }

    public void addGold(Integer amount) {
        validateRewardAmount(amount);
        this.gold += amount;
    }

    public void addGem(Integer amount) {
        validateRewardAmount(amount);
        this.gem += amount;
    }

    private void validateRewardAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.REWARD_AMOUNT_INVALID);
        }
    }
}
   

