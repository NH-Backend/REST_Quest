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
        @DisplayName("로그아웃 시 LOGGED_OUT 상태가 된다")
        void logout_changesStatusToLoggedOut() {
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
            assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.LOGGED_OUT);
            assertThat(refreshToken.isActive()).isFalse();
            assertThat(refreshToken.getLoggedOutAt()).isNotNull();
        }

        @Test
        @DisplayName("토큰 회전 시 새 refresh token 값으로 갱신된다")
        void rotate_updatesRefreshTokenValue() {
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
            refreshToken.logout();

            LocalDateTime newRefreshExpiredAt = LocalDateTime.now().plusDays(30);

            //when
            refreshToken.rotate("new-refresh-token", newRefreshExpiredAt);

            //then
            assertThat(refreshToken.getRefreshToken()).isEqualTo("new-refresh-token");
            assertThat(refreshToken.getRefreshTokenExpiredAt()).isEqualTo(newRefreshExpiredAt);
            assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
            assertThat(refreshToken.getLoggedOutAt()).isNull();
        }
    }
}
