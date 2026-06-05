package io.nh_backend.rest_quest.user.repository;

import io.nh_backend.rest_quest.user.domain.RefreshToken;
import io.nh_backend.rest_quest.user.domain.RefreshTokenStatus;
import io.nh_backend.rest_quest.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshTokenAndStatus(String refreshToken, RefreshTokenStatus status);

    List<RefreshToken> findAllByUserAndStatus(User user, RefreshTokenStatus status);

    List<RefreshToken> findAllByRefreshTokenExpiredAtBefore(LocalDateTime now);
}
