package io.nh_backend.rest_quest.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Nested
    @DisplayName("RefreshToken")
    class RefreshToken_테스트 {

        @Test
        @DisplayName("생성 시 ACTIVE 상태가 된다")
        void refreshToken_isActiveWhenCreated() {
            //given
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            RefreshToken refreshToken = RefreshToken.builder()
                    .refreshToken("refresh-token")
                    .refreshTokenExpiredAt(LocalDateTime.now().plusDays(14))
                    .user(user)
                    .build();

            //then
            assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
            assertThat(refreshToken.isActive()).isTrue();
            assertThat(refreshToken.getCreatedAt()).isNotNull();
            assertThat(refreshToken.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("로그아웃 시 INACTIVE 상태가 된다")
        void logout_changesStatusToInactive() {
            //given
            RefreshToken refreshToken = RefreshToken.builder()
                    .refreshToken("refresh-token")
                    .refreshTokenExpiredAt(LocalDateTime.now().plusDays(14))
                    .user(User.builder()
                            .email("hero@example.com")
                            .password("encoded-password")
                            .nickname("hero")
                            .role(Role.USER)
                            .build())
                    .build();

            //when
            refreshToken.logout();

            //then
            assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.INACTIVE);
            assertThat(refreshToken.isActive()).isFalse();
        }

        @Test
        @DisplayName("토큰 회전 시 기존 토큰은 INACTIVE가 되고 새 refresh token이 생성된다")
        void rotate_inactivatesCurrentTokenAndCreatesNewRefreshToken() {
            //given
            RefreshToken refreshToken = RefreshToken.builder()
                    .refreshToken("old-refresh-token")
                    .refreshTokenExpiredAt(LocalDateTime.now().plusDays(14))
                    .user(User.builder()
                            .email("hero@example.com")
                            .password("encoded-password")
                            .nickname("hero")
                            .role(Role.USER)
                            .build())
                    .build();

            LocalDateTime newRefreshExpiredAt = LocalDateTime.now().plusDays(30);

            //when
            RefreshToken rotatedRefreshToken = refreshToken.rotate("new-refresh-token", newRefreshExpiredAt);

            //then
            assertThat(refreshToken.getRefreshToken()).isEqualTo("old-refresh-token");
            assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.INACTIVE);
            assertThat(rotatedRefreshToken.getRefreshToken()).isEqualTo("new-refresh-token");
            assertThat(rotatedRefreshToken.getRefreshTokenExpiredAt()).isEqualTo(newRefreshExpiredAt);
            assertThat(rotatedRefreshToken.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
            assertThat(rotatedRefreshToken.getUser()).isSameAs(refreshToken.getUser());
        }
    }
}
