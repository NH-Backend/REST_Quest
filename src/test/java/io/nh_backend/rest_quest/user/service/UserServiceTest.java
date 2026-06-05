package io.nh_backend.rest_quest.user.service;

import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.RefreshToken;
import io.nh_backend.rest_quest.user.domain.RefreshTokenStatus;
import io.nh_backend.rest_quest.user.domain.Status;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.common.dto.KeyPair;
import io.nh_backend.rest_quest.user.dto.LoginRequest;
import io.nh_backend.rest_quest.user.dto.LoginResponse;
import io.nh_backend.rest_quest.user.dto.RefreshTokenBody;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRequest;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRotation;
import io.nh_backend.rest_quest.user.dto.UseCreateRequest;
import io.nh_backend.rest_quest.user.dto.UserResponse;
import io.nh_backend.rest_quest.user.repository.RefreshTokenRepository;
import io.nh_backend.rest_quest.user.repository.UserProfileRepository;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import io.nh_backend.rest_quest.user.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
class UserServiceTest {

    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private WalletRepository walletRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtProvider jwtProvider;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @Nested
    @DisplayName("회원가입")
    class 유저_회원가입_테스트{
        @BeforeEach
        void setUp() {
            userRepository = mock(UserRepository.class);
            userProfileRepository = mock(UserProfileRepository.class);
            walletRepository = mock(WalletRepository.class);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    jwtProvider,
                    passwordEncoder
            );
        }

        @Test
        @DisplayName("유저생성")
        void createUser_savesUserWithEncodedPasswordAndInitialData() {
            //given
            UseCreateRequest request = new UseCreateRequest(
                    "hero@example.com",
                    "password123",
                    "hero"
            );

            //when
            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.createUser(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
            ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

            verify(userRepository).save(userCaptor.capture());
            verify(userProfileRepository).save(profileCaptor.capture());
            verify(walletRepository).save(walletCaptor.capture());

            User savedUser = userCaptor.getValue();

            //then
            assertThat(savedUser.getEmail()).isEqualTo(request.email());
            assertThat(savedUser.getNickname()).isEqualTo(request.nickname());
            assertThat(savedUser.getRole()).isEqualTo(Role.USER);
            assertThat(savedUser.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(savedUser.getPassword()).isNotEqualTo(request.password());
            assertThat(passwordEncoder.matches(request.password(), savedUser.getPassword())).isTrue();

            UserProfile savedProfile = profileCaptor.getValue();
            assertThat(savedProfile.getLevel()).isEqualTo(1);
            assertThat(savedProfile.getExp()).isZero();
            assertThat(savedProfile.getUser()).isSameAs(savedUser);

            Wallet savedWallet = walletCaptor.getValue();
            assertThat(savedWallet.getGold()).isEqualTo(3000);
            assertThat(savedWallet.getGem()).isEqualTo(100);
            assertThat(savedWallet.getUser()).isSameAs(savedUser);

            assertThat(response.email()).isEqualTo(request.email());
            assertThat(response.nickname()).isEqualTo(request.nickname());
            assertThat(response.role()).isEqualTo(Role.USER);
            assertThat(response.status()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("이메일이 이미 존재하는 경우의 유저 생성")
        void createUser_throwsConflictWhenEmailAlreadyExists() {
            UseCreateRequest request = new UseCreateRequest(
                    "hero@example.com",
                    "password123",
                    "hero"
            );

            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(exception -> {
                        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                        assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(responseStatusException.getReason()).isEqualTo("이미 사용 중인 이메일입니다.");
                    });

            verify(userRepository, never()).save(any(User.class));
            verify(userProfileRepository, never()).save(any(UserProfile.class));
            verify(walletRepository, never()).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("로그인")
    class 유저_로그인_테스트 {
        @BeforeEach
        void setUp() {
            userRepository = mock(UserRepository.class);
            userProfileRepository = mock(UserProfileRepository.class);
            walletRepository = mock(WalletRepository.class);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    jwtProvider,
                    passwordEncoder
            );
        }

        @Test
        @DisplayName("이메일과 비밀번호가 일치하면 Access Token 발급 및 Refresh Token 저장")
        void login_returnsTokensAndSavesRefreshTokenWhenCredentialsAreValid() {
            //given
            String rawPassword = "password123";
            LoginRequest request = new LoginRequest("hero@example.com", rawPassword);
            User user = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(rawPassword))
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtProvider.createTokenPair(argThat(body ->
                    body.email().equals(request.email()) && body.role() == Role.USER
            ))).thenReturn(new KeyPair("access-token", "refresh-token"));
            when(jwtProvider.getAccessTokenExpiresInSeconds()).thenReturn(3600L);
            when(jwtProvider.getRefreshTokenExpiresInSeconds()).thenReturn(1209600L);

            LoginResponse response = userService.login(request);

            ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

            //then
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.accessExpiresInSeconds()).isEqualTo(3600L);
            assertThat(user.getLastLoginAt()).isNotNull();

            RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();
            assertThat(savedRefreshToken.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(savedRefreshToken.getUser()).isSameAs(user);
            assertThat(savedRefreshToken.isActive()).isTrue();
            assertThat(savedRefreshToken.getRefreshTokenExpiredAt()).isAfter(LocalDateTime.now().plusDays(13));
        }

        @Test
        @DisplayName("이메일이 존재하지 않으면 로그인 실패")
        void login_throwsUnauthorizedWhenEmailDoesNotExist() {
            //given
            LoginRequest request = new LoginRequest("unknown@example.com", "password123");

            //when
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            //then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("statusCode")
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 로그인 실패")
        void login_throwsUnauthorizedWhenPasswordDoesNotMatch() {
            //given
            LoginRequest request = new LoginRequest("hero@example.com", "wrongpassword");
            User user = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode("password123"))
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

            //then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("statusCode")
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(user.getLastLoginAt()).isNull();
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("내 계정 정보 조회")
    class 내_계정_정보_조회_테스트 {
        @BeforeEach
        void setUp() {
            userRepository = mock(UserRepository.class);
            userProfileRepository = mock(UserProfileRepository.class);
            walletRepository = mock(WalletRepository.class);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    jwtProvider,
                    passwordEncoder
            );
        }

        @Test
        @DisplayName("이메일로 현재 사용자의 계정 정보를 조회한다")
        void getMyAccount_returnsUserResponse() {
            //given
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            UserResponse response = userService.getMyAccount(user.getEmail());

            //then
            assertThat(response.email()).isEqualTo(user.getEmail());
            assertThat(response.nickname()).isEqualTo(user.getNickname());
            assertThat(response.role()).isEqualTo(Role.USER);
            assertThat(response.status()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 예외가 발생한다")
        void getMyAccount_throwsUnauthorizedWhenUserDoesNotExist() {
            //given
            String email = "unknown@example.com";

            //when
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            //then
            assertThatThrownBy(() -> userService.getMyAccount(email))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(exception -> {
                        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                        assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(responseStatusException.getReason()).isEqualTo("인증이 필요합니다.");
                    });
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class 토큰_갱신_테스트 {
        @BeforeEach
        void setUp() {
            userRepository = mock(UserRepository.class);
            userProfileRepository = mock(UserProfileRepository.class);
            walletRepository = mock(WalletRepository.class);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    jwtProvider,
                    passwordEncoder
            );
        }

        @Test
        @DisplayName("Refresh Token을 회전하고 새 토큰을 반환한다")
        void refreshToken_rotatesRefreshTokenAndReturnsNewTokens() {
            //given
            RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();
            RefreshToken savedRefreshToken = RefreshToken.builder()
                    .refreshToken(request.refreshToken())
                    .refreshTokenExpiredAt(LocalDateTime.now().plusDays(14))
                    .user(user)
                    .build();

            //when
            when(jwtProvider.parseRefreshToken(request.refreshToken()))
                    .thenReturn(new RefreshTokenBody(user.getEmail()));
            when(refreshTokenRepository.findByRefreshTokenAndStatus(
                    request.refreshToken(),
                    RefreshTokenStatus.ACTIVE
            )).thenReturn(Optional.of(savedRefreshToken));
            when(jwtProvider.createTokenPair(argThat(body ->
                    body.email().equals(user.getEmail()) && body.role() == Role.USER
            ))).thenReturn(new KeyPair("new-access-token", "new-refresh-token"));
            when(jwtProvider.getRefreshTokenExpiresInSeconds()).thenReturn(1209600L);
            when(jwtProvider.getAccessTokenExpiresInSeconds()).thenReturn(900L);

            RefreshTokenRotation response = userService.refreshToken(request);

            //then
            assertThat(savedRefreshToken.isActive()).isFalse();
            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.accessExpiresInSeconds()).isEqualTo(900L);

            ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

            RefreshToken rotatedRefreshToken = refreshTokenCaptor.getValue();
            assertThat(rotatedRefreshToken.getRefreshToken()).isEqualTo("new-refresh-token");
            assertThat(rotatedRefreshToken.getUser()).isSameAs(user);
            assertThat(rotatedRefreshToken.isActive()).isTrue();
        }

        @Test
        @DisplayName("Refresh Token이 저장소에 없으면 Unauthorized 예외가 발생한다")
        void refreshToken_throwsUnauthorizedWhenRefreshTokenDoesNotExist() {
            //given
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

            //when
            when(jwtProvider.parseRefreshToken(request.refreshToken()))
                    .thenReturn(new RefreshTokenBody("hero@example.com"));
            when(refreshTokenRepository.findByRefreshTokenAndStatus(
                    request.refreshToken(),
                    RefreshTokenStatus.ACTIVE
            )).thenReturn(Optional.empty());

            //then
            assertThatThrownBy(() -> userService.refreshToken(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(exception -> {
                        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                        assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(responseStatusException.getReason()).isEqualTo("유효하지 않은 Refresh Token입니다.");
                    });
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }
    }

}
