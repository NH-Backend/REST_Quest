package io.nh_backend.rest_quest.user.service;

import io.jsonwebtoken.JwtException;
import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.RefreshToken;
import io.nh_backend.rest_quest.user.domain.RefreshTokenStatus;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.user.dto.AccessTokenBody;
import io.nh_backend.rest_quest.user.dto.LoginRequest;
import io.nh_backend.rest_quest.user.dto.LoginResponse;
import io.nh_backend.rest_quest.user.dto.RefreshTokenBody;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRequest;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRotation;
import io.nh_backend.rest_quest.user.dto.UseCreateRequest;
import io.nh_backend.rest_quest.user.dto.UserResponse;
import io.nh_backend.rest_quest.common.dto.KeyPair;
import io.nh_backend.rest_quest.user.repository.RefreshTokenRepository;
import io.nh_backend.rest_quest.user.repository.UserProfileRepository;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import io.nh_backend.rest_quest.user.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WalletRepository walletRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UseCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.UNVALID_EMAIL_ADDRESS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        userProfileRepository.save(UserProfile.builder()
                .level(1)
                .user(savedUser)
                .build());
        walletRepository.save(Wallet.builder()
                .user(savedUser)
                .build());

        return toResponse(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_LOGIN_INFORMATION
                ));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOGIN_INFORMATION
            );
        }

        user.recordLogin();
        KeyPair keyPair = jwtProvider.createTokenPair(
                new AccessTokenBody(user.getEmail(), user.getRole())
        );

        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(keyPair.refreshToken())
                .refreshTokenExpiredAt(LocalDateTime.now().plusSeconds(
                        jwtProvider.getRefreshTokenExpiresInSeconds()
                ))
                .user(user)
                .build());

        return new LoginResponse(
                keyPair.accessToken(),
                keyPair.refreshToken(),
                jwtProvider.getAccessTokenExpiresInSeconds()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getMyAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UNAUTHORIZED_USER
                ));

        return toResponse(user);
    }

    @Transactional
    public RefreshTokenRotation refreshToken(RefreshTokenRequest request) {
        RefreshTokenBody refreshTokenBody = parseRefreshToken(request.refreshToken());
        RefreshToken savedRefreshToken = refreshTokenRepository
                .findByRefreshTokenAndStatus(request.refreshToken(), RefreshTokenStatus.ACTIVE)
                .orElseThrow(this::invalidRefreshTokenException);

        if (savedRefreshToken.getRefreshTokenExpiredAt().isBefore(LocalDateTime.now())) {
            savedRefreshToken.expire();
            throw invalidRefreshTokenException();
        }

        User user = savedRefreshToken.getUser();
        if (!user.getEmail().equals(refreshTokenBody.email())) {
            throw invalidRefreshTokenException();
        }

        KeyPair keyPair = jwtProvider.createTokenPair(
                new AccessTokenBody(user.getEmail(), user.getRole())
        );
        RefreshToken rotatedRefreshToken = savedRefreshToken.rotate(
                keyPair.refreshToken(),
                LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpiresInSeconds())
        );
        refreshTokenRepository.save(rotatedRefreshToken);

        return new RefreshTokenRotation(
                keyPair.accessToken(),
                keyPair.refreshToken(),
                jwtProvider.getAccessTokenExpiresInSeconds()
        );
    }

    private RefreshTokenBody parseRefreshToken(String refreshToken) {
        try {
            return jwtProvider.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidRefreshTokenException();
        }
    }

    private BusinessException invalidRefreshTokenException() {
        return new BusinessException(
                ErrorCode.UNVALID_REFRESH_TOKEN
        );
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
