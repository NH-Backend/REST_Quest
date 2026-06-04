package io.nh_backend.rest_quest.user.domain;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Refresh_Token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime refreshTokenExpired;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Token(String refreshToken, LocalDateTime refreshTokenExpired, User user) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpired = refreshTokenExpired;
        this.user = user;
    }
}