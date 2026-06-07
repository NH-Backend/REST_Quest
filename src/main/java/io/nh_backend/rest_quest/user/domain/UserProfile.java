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

    private Long exp = 0L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserProfile(Integer level, Long exp, User user) {
        this.level = level;
        this.exp = exp == null ? 0L : exp;
        this.user = user;
    }
}
