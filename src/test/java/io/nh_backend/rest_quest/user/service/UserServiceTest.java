package io.nh_backend.rest_quest.user.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.dto.KeyPair;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.friend_request.domain.FriendStatus;
import io.nh_backend.rest_quest.friend_request.repository.FriendRequestRepository;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.item.repository.UserItemRepository;
import io.nh_backend.rest_quest.user.domain.RefreshToken;
import io.nh_backend.rest_quest.user.domain.RefreshTokenStatus;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.Status;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.user.dto.LoginRequest;
import io.nh_backend.rest_quest.user.dto.LoginResponse;
import io.nh_backend.rest_quest.user.dto.RefreshTokenBody;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRequest;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRotation;
import io.nh_backend.rest_quest.user.dto.UseCreateRequest;
import io.nh_backend.rest_quest.user.dto.UserDataResponse;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private UserItemRepository userItemRepository;
    private FriendRequestRepository friendRequestRepository;
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
            userItemRepository = mock(UserItemRepository.class);
            friendRequestRepository = mock(FriendRequestRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    userItemRepository,
                    friendRequestRepository,
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

            assertBusinessException(
                    () -> userService.createUser(request),
                    ErrorCode.UNVALID_EMAIL_ADDRESS
            );

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
            userItemRepository = mock(UserItemRepository.class);
            friendRequestRepository = mock(FriendRequestRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    userItemRepository,
                    friendRequestRepository,
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
            assertBusinessException(
                    () -> userService.login(request),
                    ErrorCode.INVALID_LOGIN_INFORMATION
            );
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
            assertBusinessException(
                    () -> userService.login(request),
                    ErrorCode.INVALID_LOGIN_INFORMATION
            );
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
            userItemRepository = mock(UserItemRepository.class);
            friendRequestRepository = mock(FriendRequestRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    userItemRepository,
                    friendRequestRepository,
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
            assertBusinessException(
                    () -> userService.getMyAccount(email),
                    ErrorCode.UNAUTHORIZED_USER
            );
        }

        @Test
        @DisplayName("INF_UNITY_007: 계정, 프로필, 지갑, 인벤토리, 친구 수를 통합 조회한다")
        void getMyData_returnsAccountProfileWalletInventoryAndFriendCount() {
            //given
            User user = User.builder()
                    .email("gamer@test.com")
                    .password("encoded-password")
                    .nickname("게이머")
                    .role(Role.USER)
                    .build();
            ReflectionTestUtils.setField(user, "id", 5L);
            ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0));
            ReflectionTestUtils.setField(user, "lastLoginAt", LocalDateTime.of(2026, 5, 21, 10, 0));

            UserProfile profile = UserProfile.builder()
                    .level(10)
                    .exp(500)
                    .user(user)
                    .build();
            Wallet wallet = Wallet.builder()
                    .gold(5000)
                    .gem(10)
                    .user(user)
                    .build();
            Item item = Item.builder()
                    .itemName("연습용 검")
                    .rId("sword_001")
                    .description("초보자용 검입니다.")
                    .goldPrice(100)
                    .gemPrice(0)
                    .sellPrice(50)
                    .expCoupon(0)
                    .gemCoupon(0)
                    .goldCoupon(0)
                    .itemType(ItemType.WEAPON)
                    .itemGrade(ItemGrade.COMMON)
                    .build();
            ReflectionTestUtils.setField(item, "id", 1L);
            UserItem userItem = UserItem.builder()
                    .user(user)
                    .item(item)
                    .quantity(1)
                    .equipped(true)
                    .build();
            ReflectionTestUtils.setField(userItem, "id", 1L);
            ReflectionTestUtils.setField(userItem, "acquiredAt", LocalDateTime.of(2025, 1, 1, 0, 0));

            //when
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(userProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
            when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
            when(userItemRepository.findAllByUserAndDeletedAtIsNull(user)).thenReturn(List.of(userItem));
            when(friendRequestRepository.countActiveFriends(user, FriendStatus.ACCEPTED)).thenReturn(3L);

            UserDataResponse response = userService.getMyData(user.getEmail());

            //then
            assertThat(response.account().id()).isEqualTo(5L);
            assertThat(response.account().email()).isEqualTo("gamer@test.com");
            assertThat(response.account().nickname()).isEqualTo("게이머");
            assertThat(response.account().role()).isEqualTo(Role.USER);
            assertThat(response.account().status()).isEqualTo(Status.ACTIVE);
            assertThat(response.account().createdAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
            assertThat(response.account().lastLoginAt()).isEqualTo(LocalDateTime.of(2026, 5, 21, 10, 0));
            assertThat(response.profile().level()).isEqualTo(10);
            assertThat(response.profile().exp()).isEqualTo(500);
            assertThat(response.wallet().gold()).isEqualTo(5000);
            assertThat(response.wallet().gem()).isEqualTo(10);
            assertThat(response.inventory()).hasSize(1);
            assertThat(response.inventory().get(0).userItemId()).isEqualTo(1L);
            assertThat(response.inventory().get(0).itemId()).isEqualTo(1L);
            assertThat(response.inventory().get(0).rId()).isEqualTo("sword_001");
            assertThat(response.inventory().get(0).itemName()).isEqualTo("연습용 검");
            assertThat(response.inventory().get(0).itemType()).isEqualTo("WEAPON");
            assertThat(response.inventory().get(0).itemGrade()).isEqualTo("COMMON");
            assertThat(response.inventory().get(0).description()).isEqualTo("초보자용 검입니다.");
            assertThat(response.inventory().get(0).goldPrice()).isEqualTo(100);
            assertThat(response.inventory().get(0).gemPrice()).isEqualTo(0);
            assertThat(response.inventory().get(0).sellPrice()).isEqualTo(50);
            assertThat(response.inventory().get(0).quantity()).isEqualTo(1);
            assertThat(response.inventory().get(0).equipped()).isTrue();
            assertThat(response.inventory().get(0).acquiredAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
            assertThat(response.friendCount()).isEqualTo(3L);

            verify(friendRequestRepository).countActiveFriends(user, FriendStatus.ACCEPTED);
        }
    }

    @Nested
    @DisplayName("로그인 상태 판별")
    class 로그인_상태_판별_테스트 {
        @BeforeEach
        void setUp() {
            userRepository = mock(UserRepository.class);
            userProfileRepository = mock(UserProfileRepository.class);
            walletRepository = mock(WalletRepository.class);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            userItemRepository = mock(UserItemRepository.class);
            friendRequestRepository = mock(FriendRequestRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    userItemRepository,
                    friendRequestRepository,
                    jwtProvider,
                    passwordEncoder
            );
        }

        @Test
        @DisplayName("ACTIVE Refresh Token이 있으면 로그인 상태를 true로 반환한다")
        void isLoggedIn_returnsTrueWhenActiveRefreshTokenExists() {
            //given
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.existsByUserAndStatus(user, RefreshTokenStatus.ACTIVE))
                    .thenReturn(true);

            boolean loggedIn = userService.isLoggedIn(user.getEmail());

            //then
            assertThat(loggedIn).isTrue();
        }

        @Test
        @DisplayName("ACTIVE Refresh Token이 없으면 로그인 상태를 false로 반환한다")
        void isLoggedIn_returnsFalseWhenActiveRefreshTokenDoesNotExist() {
            //given
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.existsByUserAndStatus(user, RefreshTokenStatus.ACTIVE))
                    .thenReturn(false);

            boolean loggedIn = userService.isLoggedIn(user.getEmail());

            //then
            assertThat(loggedIn).isFalse();
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 예외가 발생한다")
        void isLoggedIn_throwsUnauthorizedWhenUserDoesNotExist() {
            //given
            String email = "unknown@example.com";

            //when
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            //then
            assertBusinessException(
                    () -> userService.isLoggedIn(email),
                    ErrorCode.UNAUTHORIZED_USER
            );
            verify(refreshTokenRepository, never()).existsByUserAndStatus(any(User.class), any(RefreshTokenStatus.class));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class 로그아웃_테스트 {
        @BeforeEach
        void setUp() {
            userRepository = mock(UserRepository.class);
            userProfileRepository = mock(UserProfileRepository.class);
            walletRepository = mock(WalletRepository.class);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            userItemRepository = mock(UserItemRepository.class);
            friendRequestRepository = mock(FriendRequestRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    userItemRepository,
                    friendRequestRepository,
                    jwtProvider,
                    passwordEncoder
            );
        }

        @Test
        @DisplayName("INF_UNITY_004: 서버 측 Refresh Token을 삭제하고 로그아웃한다")
        void logout_deletesActiveRefreshTokensForAuthenticatedUser() {
            //given
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();
            RefreshToken firstRefreshToken = RefreshToken.builder()
                    .refreshToken("first-refresh-token")
                    .refreshTokenExpiredAt(LocalDateTime.now().plusDays(14))
                    .user(user)
                    .build();
            RefreshToken secondRefreshToken = RefreshToken.builder()
                    .refreshToken("second-refresh-token")
                    .refreshTokenExpiredAt(LocalDateTime.now().plusDays(14))
                    .user(user)
                    .build();
            List<RefreshToken> activeRefreshTokens = List.of(firstRefreshToken, secondRefreshToken);

            //when
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.findAllByUserAndStatus(user, RefreshTokenStatus.ACTIVE))
                    .thenReturn(activeRefreshTokens);

            userService.logout(user.getEmail());

            //then
            verify(refreshTokenRepository).deleteAll(activeRefreshTokens);
        }

        @Test
        @DisplayName("활성 Refresh Token이 없어도 로그아웃 요청은 성공한다")
        void logout_succeedsWhenActiveRefreshTokenDoesNotExist() {
            //given
            User user = User.builder()
                    .email("hero@example.com")
                    .password("encoded-password")
                    .nickname("hero")
                    .role(Role.USER)
                    .build();

            //when
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.findAllByUserAndStatus(user, RefreshTokenStatus.ACTIVE))
                    .thenReturn(List.of());

            userService.logout(user.getEmail());

            //then
            verify(refreshTokenRepository).deleteAll(List.of());
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 예외가 발생한다")
        void logout_throwsUnauthorizedWhenUserDoesNotExist() {
            //given
            String email = "unknown@example.com";

            //then
            assertBusinessException(
                    () -> userService.logout(email),
                    ErrorCode.UNAUTHORIZED_USER
            );
            verify(refreshTokenRepository, never()).deleteAll(any());
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
            userItemRepository = mock(UserItemRepository.class);
            friendRequestRepository = mock(FriendRequestRepository.class);
            jwtProvider = mock(JwtProvider.class);
            passwordEncoder = new BCryptPasswordEncoder();
            userService = new UserService(
                    userRepository,
                    userProfileRepository,
                    walletRepository,
                    refreshTokenRepository,
                    userItemRepository,
                    friendRequestRepository,
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
            assertBusinessException(
                    () -> userService.refreshToken(request),
                    ErrorCode.UNVALID_REFRESH_TOKEN
            );
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }
    }

    private static void assertBusinessException(Runnable action, ErrorCode expectedErrorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(expectedErrorCode);
                    assertThat(businessException.getMessage()).isEqualTo(expectedErrorCode.getDescription());
                });
    }

}
