package io.nh_backend.rest_quest.common.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.dto.AccessTokenBody;
import io.nh_backend.rest_quest.user.service.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("JWT 인증 필터")
    class JwtAuthenticationFilter_테스트 {

        @Test
        @DisplayName("Bearer Access Token이 있으면 SecurityContext에 인증 정보를 저장한다")
        void doFilter_setsAuthenticationWhenBearerTokenExists() throws Exception {
            //given
            JwtProvider jwtProvider = mock(JwtProvider.class);
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            request.addHeader("Authorization", "Bearer access-token");
            when(jwtProvider.parseAccessToken("access-token"))
                    .thenReturn(new AccessTokenBody("hero@example.com", Role.USER));

            //when
            filter.doFilter(request, response, filterChain);

            //then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("hero@example.com");
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 인증 정보를 저장하지 않는다")
        void doFilter_doesNotAuthenticateWhenAuthorizationHeaderDoesNotExist() throws Exception {
            //given
            JwtProvider jwtProvider = mock(JwtProvider.class);
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            //when
            filter.doFilter(request, response, filterChain);

            //then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtProvider, never()).parseAccessToken("access-token");
        }

        @Test
        @DisplayName("Bearer 형식이 아니면 인증 정보를 저장하지 않는다")
        void doFilter_doesNotAuthenticateWhenAuthorizationHeaderIsNotBearer() throws Exception {
            //given
            JwtProvider jwtProvider = mock(JwtProvider.class);
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            request.addHeader("Authorization", "Basic access-token");

            //when
            filter.doFilter(request, response, filterChain);

            //then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtProvider, never()).parseAccessToken("access-token");
        }

        @Test
        @DisplayName("Access Token이 만료되면 401 응답을 반환한다")
        void doFilter_returnsUnauthorizedWhenAccessTokenIsExpired() throws Exception {
            //given
            JwtProvider jwtProvider = mock(JwtProvider.class);
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            request.addHeader("Authorization", "Bearer expired-token");
            when(jwtProvider.parseAccessToken("expired-token"))
                    .thenThrow(new ExpiredJwtException(
                            Jwts.header().build(),
                            Jwts.claims().build(),
                            "expired"
                    ));

            //when
            filter.doFilter(request, response, filterChain);

            //then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains(ErrorCode.EXPIRED_ACCESS_TOKEN.getDescription());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(filterChain.getRequest()).isNull();
        }

        @Test
        @DisplayName("Access Token 서명 또는 형식이 유효하지 않으면 401 응답을 반환한다")
        void doFilter_returnsUnauthorizedWhenAccessTokenIsInvalid() throws Exception {
            //given
            JwtProvider jwtProvider = mock(JwtProvider.class);
            JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider);
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            request.addHeader("Authorization", "Bearer invalid-token");
            when(jwtProvider.parseAccessToken("invalid-token"))
                    .thenThrow(new JwtException("invalid"));

            //when
            filter.doFilter(request, response, filterChain);

            //then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains(ErrorCode.INVALID_ACCESS_TOKEN.getDescription());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(filterChain.getRequest()).isNull();
        }
    }
}
