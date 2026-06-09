package io.nh_backend.rest_quest.user.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.nh_backend.rest_quest.common.dto.JwtProperties;
import io.nh_backend.rest_quest.common.dto.KeyPair;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.dto.AccessTokenBody;
import io.nh_backend.rest_quest.user.dto.RefreshTokenBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final Clock clock;

    @Autowired
    public JwtProvider(JwtProperties jwtProperties) {
        this(jwtProperties, Clock.systemUTC());
    }

    JwtProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecrets().getAppKey()));
        this.clock = clock;
    }

    public KeyPair createTokenPair(AccessTokenBody accessTokenBody) {
        return new KeyPair(
                createAccessToken(accessTokenBody),
                createRefreshToken(new RefreshTokenBody(accessTokenBody.email()))
        );
    }

    public String createAccessToken(AccessTokenBody body) {
        Instant now = clock.instant();
        //Instant expiresAt = now.plusSeconds(jwtProperties.getValidations().getAccess());

        return Jwts.builder()
                .issuer(jwtProperties.getPayload().getIssuer())
                .subject(jwtProperties.getPayload().getSubjectAccessToken())
                .audience().add(jwtProperties.getPayload().getAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(new Date(new Date().getTime() + jwtProperties.getValidations().getAccess()))
                .claim(EMAIL_CLAIM, body.email())
                .claim(ROLE_CLAIM, body.role().name())
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(RefreshTokenBody body) {
        Instant now = clock.instant();
        Instant expiresAt = now.plusSeconds(jwtProperties.getValidations().getRefresh());

        return Jwts.builder()
                .issuer(jwtProperties.getPayload().getIssuer())
                .subject(jwtProperties.getPayload().getSubjectRefreshToken())
                .audience().add(jwtProperties.getPayload().getAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(EMAIL_CLAIM, body.email())
                .signWith(secretKey)
                .compact();
    }

    public AccessTokenBody parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        validateSubject(claims, jwtProperties.getPayload().getSubjectAccessToken());

        return new AccessTokenBody(
                claims.get(EMAIL_CLAIM, String.class),
                Role.valueOf(claims.get(ROLE_CLAIM, String.class))
        );
    }

    public RefreshTokenBody parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        validateSubject(claims, jwtProperties.getPayload().getSubjectRefreshToken());

        return new RefreshTokenBody(claims.get(EMAIL_CLAIM, String.class));
    }

    public long getAccessTokenExpiresInSeconds() {
        return jwtProperties.getValidations().getAccess();
    }

    public long getRefreshTokenExpiresInSeconds() {
        return jwtProperties.getValidations().getRefresh();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getPayload().getIssuer())
                .requireAudience(jwtProperties.getPayload().getAudience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateSubject(Claims claims, String expectedSubject) {
        if (!expectedSubject.equals(claims.getSubject())) {
            throw new IllegalArgumentException("토큰 subject가 올바르지 않습니다.");
        }
    }
}
