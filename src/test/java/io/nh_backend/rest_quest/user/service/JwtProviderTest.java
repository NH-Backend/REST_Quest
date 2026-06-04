package io.nh_backend.rest_quest.user.service;

import io.nh_backend.rest_quest.common.dto.JwtProperties;
import io.nh_backend.rest_quest.common.dto.KeyPair;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.dto.AccessTokenBody;
import io.nh_backend.rest_quest.user.dto.RefreshTokenBody;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class JwtProviderTest {

    private JwtProvider jwtProvider;

    @Nested
    @DisplayName("JWT Provider")
    class JwtProvider_테스트 {

        @BeforeEach
        void setUp() {
            JwtProperties jwtProperties = new JwtProperties(
                    new JwtProperties.Payload(
                            "REST_Quest",
                            "ACCESS_TOKEN",
                            "REFRESH_TOKEN",
                            "REST_Quest_USER"
                    ),
                    new JwtProperties.Secrets(
                            "cmVzdC1xdWVzdC1qd3Qtc2VjcmV0LWtleS1tdXN0LWJlLWxvbmc=",
                            "REST_Quest"
                    ),
                    new JwtProperties.Validations(3600, 1209600)
            );
            Clock clock = Clock.fixed(
                    Instant.parse("2030-06-04T10:00:00Z"),
                    ZoneOffset.UTC
            );
            jwtProvider = new JwtProvider(jwtProperties, clock);
        }

        @Test
        @DisplayName("Access Token 생성 후 파싱")
        void createAccessToken_parsesAccessTokenBody() {
            //given
            AccessTokenBody body = new AccessTokenBody("hero@example.com", Role.USER);

            //when
            String token = jwtProvider.createAccessToken(body);
            AccessTokenBody parsedBody = jwtProvider.parseAccessToken(token);

            //then
            assertThat(token).isNotBlank();
            log.info("token : " + token);
            assertThat(parsedBody.email()).isEqualTo(body.email());
            assertThat(parsedBody.role()).isEqualTo(body.role());
        }

        @Test
        @DisplayName("Refresh Token 생성 후 파싱")
        void createRefreshToken_parsesRefreshTokenBody() {
            //given
            RefreshTokenBody body = new RefreshTokenBody("hero@example.com");

            //when
            String token = jwtProvider.createRefreshToken(body);
            RefreshTokenBody parsedBody = jwtProvider.parseRefreshToken(token);

            //then
            assertThat(token).isNotBlank();
            assertThat(parsedBody.email()).isEqualTo(body.email());
        }

        @Test
        @DisplayName("Access/Refresh Token Pair 생성")
        void createTokenPair_returnsAccessAndRefreshTokens() {
            //given
            AccessTokenBody body = new AccessTokenBody("hero@example.com", Role.USER);

            //when
            KeyPair keyPair = jwtProvider.createTokenPair(body);

            //then
            assertThat(jwtProvider.parseAccessToken(keyPair.accessToken()).email()).isEqualTo(body.email());
            assertThat(jwtProvider.parseRefreshToken(keyPair.refreshToken()).email()).isEqualTo(body.email());
            assertThat(jwtProvider.getAccessTokenExpiresInSeconds()).isEqualTo(3600);
            assertThat(jwtProvider.getRefreshTokenExpiresInSeconds()).isEqualTo(1209600);
        }

        @Test
        @DisplayName("Refresh Token을 Access Token으로 파싱하면 실패")
        void parseAccessToken_throwsWhenTokenSubjectIsRefreshToken() {
            //given
            String refreshToken = jwtProvider.createRefreshToken(
                    new RefreshTokenBody("hero@example.com")
            );

            //when, then
            assertThatThrownBy(() -> jwtProvider.parseAccessToken(refreshToken))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
