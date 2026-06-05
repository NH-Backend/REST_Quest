package io.nh_backend.rest_quest.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime refreshTokenExpiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefreshTokenStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public RefreshToken(
            String refreshToken,
            LocalDateTime refreshTokenExpiredAt,
            User user
    ) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiredAt = refreshTokenExpiredAt;
        this.user = user;
        this.status = RefreshTokenStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == RefreshTokenStatus.ACTIVE;
    }

    public void logout() {
        this.status = RefreshTokenStatus.INACTIVE;
    }

    public void expire() {
        this.status = RefreshTokenStatus.INACTIVE;
    }

    public RefreshToken rotate(String refreshToken, LocalDateTime refreshTokenExpiredAt) {
        expire();

        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .refreshTokenExpiredAt(refreshTokenExpiredAt)
                .user(user)
                .build();
    }
}
