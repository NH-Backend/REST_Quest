package io.nh_backend.rest_quest.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "user_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer level;

    private Integer exp = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserProfile(Integer level, Integer exp, User user) {
        this.level = level;
        this.exp = exp == null ? 0 : exp;
        this.user = user;
    }
}
