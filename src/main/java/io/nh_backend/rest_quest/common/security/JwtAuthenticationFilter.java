package io.nh_backend.rest_quest.common.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.user.dto.AccessTokenBody;
import io.nh_backend.rest_quest.user.service.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);

        if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                authenticate(accessToken);
            } catch (ExpiredJwtException exception) {
                reject(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
                return;
            } catch (JwtException | IllegalArgumentException exception) {
                reject(response, ErrorCode.INVALID_ACCESS_TOKEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }

    private void authenticate(String accessToken) {
        AccessTokenBody accessTokenBody = jwtProvider.parseAccessToken(accessToken);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        accessTokenBody.email(),
                        null,
                        List.of(new SimpleGrantedAuthority(ROLE_PREFIX + accessTokenBody.role().name()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void reject(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                  "success": false,
                  "message": "해당 요청이 실패되었습니다.",
                  "data": null                  
                }
                """
        );
    }
}
