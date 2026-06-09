package io.nh_backend.rest_quest.user.controller;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.eventhandler.GlobalExceptionHandler;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.user.domain.Provider;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.Status;
import io.nh_backend.rest_quest.user.dto.LoginRequest;
import io.nh_backend.rest_quest.user.dto.LoginResponse;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRequest;
import io.nh_backend.rest_quest.user.dto.RefreshTokenRotation;
import io.nh_backend.rest_quest.user.dto.ShowWalletResponse;
import io.nh_backend.rest_quest.user.dto.UseCreateRequest;
import io.nh_backend.rest_quest.user.dto.UserDataResponse;
import io.nh_backend.rest_quest.user.dto.UserProfileResponse;
import io.nh_backend.rest_quest.user.dto.UserResponse;
import io.nh_backend.rest_quest.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @Nested
    @DisplayName("회원가입")
    class 유저_회원가입_테스트 {
        @BeforeEach
        void setUp() {
            userService = mock(UserService.class);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            mockMvc = MockMvcBuilders
                    .standaloneSetup(new UserController(userService))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("CreatedUserResponse 반환하는 회원가입 테스트")
        void signup_returnsCreatedUserResponse() throws Exception {
            //given
            UseCreateRequest request = new UseCreateRequest(
                    "hero@example.com",
                    "password123",
                    "hero"
            );
            UserResponse response = new UserResponse(
                    1L,
                    request.email(),
                    request.nickname(),
                    Role.USER,
                    Status.ACTIVE,
                    Provider.LOCAL,
                    LocalDateTime.of(2026, 6, 4, 18, 0),
                    null
            );
            //when
            when(userService.createUser(request)).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "hero@example.com",
                                      "password": "password123",
                                      "nickname": "hero"
                                    }
                                    """))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.USER_CREATED.getSuccessMessage()))
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.email").value(request.email()))
                    .andExpect(jsonPath("$.data.nickname").value(request.nickname()))
                    .andExpect(jsonPath("$.data.role").value(Role.USER.name()))
                    .andExpect(jsonPath("$.data.status").value(Status.ACTIVE.name()))
                    .andExpect(jsonPath("$.data.provider").value(Provider.LOCAL.name()))
                    .andExpect(jsonPath("$.data.profileImageUrl").doesNotExist())
                    .andExpect(jsonPath("$.data.createdAt").value("2026-06-04T18:00:00"))
                    .andExpect(jsonPath("$.data.lastLoginAt").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("비밀번호가 8자 미만이면 Bad Request 응답을 반환한다")
        void signup_returnsBadRequestWhenPasswordIsTooShort() throws Exception {
            //given
            UseCreateRequest request = new UseCreateRequest(
                    "hero@example.com",
                    "short",
                    "hero"
            );

            //when
            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "hero@example.com",
                                      "password": "short",
                                      "nickname": "hero"
                                    }
                                    """))
            //then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_BAD_REQUEST.getDescription()))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("이미 사용 중인 이메일이면 Conflict 응답을 반환한다")
        void signup_returnsConflictWhenEmailAlreadyExists() throws Exception {
            //given
            UseCreateRequest request = new UseCreateRequest(
                    "hero@example.com",
                    "password123",
                    "hero"
            );

            //when
            when(userService.createUser(request)).thenThrow(new BusinessException(
                    ErrorCode.UNVALID_EMAIL_ADDRESS
            ));

            mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "hero@example.com",
                                      "password": "password123",
                                      "nickname": "hero"
                                    }
                                    """))
            //then
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Nested
    @DisplayName("로그인")
    class 유저_로그인_테스트 {
        @BeforeEach
        void setUp() {
            userService = mock(UserService.class);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            mockMvc = MockMvcBuilders
                    .standaloneSetup(new UserController(userService))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("LoginResponse 반환하는 로그인 테스트")
        void login_returnsLoginResponse() throws Exception {
            //given
            LoginRequest request = new LoginRequest(
                    "hero@example.com",
                    "password123"
            );
            LoginResponse response = new LoginResponse(
                    "access-token",
                    "refresh-token",
                    3600L
            );

            //when
            when(userService.login(request)).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "hero@example.com",
                                      "password": "password123"
                                    }
                                    """))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.USER_LOGIN.getSuccessMessage()))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.data.accessExpiresInSeconds").value(3600L))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("request가 올바르지 않을 때 bad Request 반환")
        void login_returnsBadRequestWhenRequestIsInvalid() throws Exception {
            //given
            LoginRequest request = new LoginRequest(
                    "invalid-email",
                    "short"
            );

            //when
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "invalid-email",
                                      "password": "short"
                                    }
                                    """))
            //then
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이메일 또는 비밀번호가 올바르지 않으면 Unauthorized 응답을 반환한다")
        void login_returnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
            //given
            LoginRequest request = new LoginRequest(
                    "hero@example.com",
                    "wrongpassword"
            );

            //when
            when(userService.login(request)).thenThrow(new BusinessException(
                    ErrorCode.INVALID_LOGIN_INFORMATION
            ));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "hero@example.com",
                                      "password": "wrongpassword"
                                    }
                                    """))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Nested
    @DisplayName("내 계정 정보 조회")
    class 내_계정_정보_조회_테스트 {
        @BeforeEach
        void setUp() {
            userService = mock(UserService.class);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            mockMvc = MockMvcBuilders
                    .standaloneSetup(new UserController(userService))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("인증된 사용자의 계정 정보를 반환한다")
        void getMyAccount_returnsUserResponse() throws Exception {
            //given
            String email = "hero@example.com";
            UserResponse response = new UserResponse(
                    1L,
                    email,
                    "hero",
                    Role.USER,
                    Status.ACTIVE,
                    Provider.LOCAL,
                    LocalDateTime.of(2026, 6, 4, 18, 0),
                    LocalDateTime.of(2026, 6, 5, 10, 0)
            );

            //when
            when(userService.getMyAccount(email)).thenReturn(response);

            mockMvc.perform(get("/api/v1/users/me")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.email").value(email))
                    .andExpect(jsonPath("$.data.nickname").value("hero"))
                    .andExpect(jsonPath("$.data.role").value(Role.USER.name()))
                    .andExpect(jsonPath("$.data.status").value(Status.ACTIVE.name()))
                    .andExpect(jsonPath("$.data.provider").value(Provider.LOCAL.name()))
                    .andExpect(jsonPath("$.data.profileImageUrl").doesNotExist())
                    .andExpect(jsonPath("$.data.createdAt").value("2026-06-04T18:00:00"))
                    .andExpect(jsonPath("$.data.lastLoginAt").value("2026-06-05T10:00:00"))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 응답을 반환한다")
        void getMyAccount_returnsUnauthorizedWhenUserDoesNotExist() throws Exception {
            //given
            String email = "hero@example.com";

            //when
            when(userService.getMyAccount(email)).thenThrow(new BusinessException(
                    ErrorCode.UNAUTHORIZED_USER
            ));

            mockMvc.perform(get("/api/v1/users/me")
                            .principal(() -> email))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Nested
    @DisplayName("내 통합 데이터 조회")
    class 내_통합_데이터_조회_테스트 {
        @BeforeEach
        void setUp() {
            userService = mock(UserService.class);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            mockMvc = MockMvcBuilders
                    .standaloneSetup(new UserController(userService))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("INF_UNITY_007: 인증된 사용자의 통합 데이터를 반환한다")
        void getMyData_returnsUserDataResponse() throws Exception {
            //given
            String email = "hero@example.com";
            UserDataResponse response = new UserDataResponse(
                    new UserResponse(
                            5L,
                            "gamer@test.com",
                            "게이머",
                            Role.USER,
                            Status.ACTIVE,
                            Provider.LOCAL,
                            LocalDateTime.of(2025, 1, 1, 0, 0),
                            LocalDateTime.of(2026, 5, 21, 10, 0)
                    ),
                    new UserProfileResponse(10, 500L),
                    new ShowWalletResponse(5000L, 10L),
                    List.of(new UserItemResponse(
                            1L,
                            1L,
                            "sword_001",
                            "연습용 검",
                            "WEAPON",
                            "COMMON",
                            "초보자용 검입니다.",
                            100,
                            50,
                            50,
                            1,
                            true,
                            "2025-01-01T00:00:00"
                    )),
                    3L
            );

            //when
            when(userService.getMyData(email)).thenReturn(response);

            mockMvc.perform(get("/api/v1/users/me/data")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data.account.userId").value(5L))
                    .andExpect(jsonPath("$.data.account.email").value("gamer@test.com"))
                    .andExpect(jsonPath("$.data.account.nickname").value("게이머"))
                    .andExpect(jsonPath("$.data.account.role").value(Role.USER.name()))
                    .andExpect(jsonPath("$.data.account.status").value(Status.ACTIVE.name()))
                    .andExpect(jsonPath("$.data.account.provider").value(Provider.LOCAL.name()))
                    .andExpect(jsonPath("$.data.account.profileImageUrl").doesNotExist())
                    .andExpect(jsonPath("$.data.account.createdAt").value("2025-01-01T00:00:00"))
                    .andExpect(jsonPath("$.data.account.lastLoginAt").value("2026-05-21T10:00:00"))
                    .andExpect(jsonPath("$.data.profile.level").value(10))
                    .andExpect(jsonPath("$.data.profile.exp").value(500))
                    .andExpect(jsonPath("$.data.profile.totalPlaySeconds").doesNotExist())
                    .andExpect(jsonPath("$.data.wallet.gold").value(5000))
                    .andExpect(jsonPath("$.data.wallet.gem").value(10))
                    .andExpect(jsonPath("$.data.inventory[0].userItemId").value(1L))
                    .andExpect(jsonPath("$.data.inventory[0].itemId").value(1L))
                    .andExpect(jsonPath("$.data.inventory[0].rId").value("sword_001"))
                    .andExpect(jsonPath("$.data.inventory[0].itemName").value("연습용 검"))
                    .andExpect(jsonPath("$.data.inventory[0].itemType").value("WEAPON"))
                    .andExpect(jsonPath("$.data.inventory[0].itemGrade").value("COMMON"))
                    .andExpect(jsonPath("$.data.inventory[0].description").value("초보자용 검입니다."))
                    .andExpect(jsonPath("$.data.inventory[0].price").value(100))
                    .andExpect(jsonPath("$.data.inventory[0].gemPrice").value(50))
                    .andExpect(jsonPath("$.data.inventory[0].sellPrice").value(50))
                    .andExpect(jsonPath("$.data.inventory[0].quantity").value(1))
                    .andExpect(jsonPath("$.data.inventory[0].equipped").value(true))
                    .andExpect(jsonPath("$.data.inventory[0].acquiredAt").value("2025-01-01T00:00:00"))
                    .andExpect(jsonPath("$.data.friendCount").value(3L))
                    .andExpect(jsonPath("$.data.level").doesNotExist())
                    .andExpect(jsonPath("$.data.exp").doesNotExist())
                    .andExpect(jsonPath("$.data.gold").doesNotExist())
                    .andExpect(jsonPath("$.data.gem").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).getMyData(email);
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 통합 데이터를 조회할 수 없다")
        void getMyData_returnsUnauthorizedWhenPrincipalDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/data"))
            //then
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 응답을 반환한다")
        void getMyData_returnsUnauthorizedWhenUserDoesNotExist() throws Exception {
            //given
            String email = "hero@example.com";

            //when
            when(userService.getMyData(email)).thenThrow(new BusinessException(
                    ErrorCode.UNAUTHORIZED_USER
            ));

            mockMvc.perform(get("/api/v1/users/me/data")
                            .principal(() -> email))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("INF_UNITY_023: 인증된 사용자의 프로필을 반환한다")
        void getMyProfile_returnsProfile() throws Exception {
            //given
            String email = "hero@example.com";
            UserProfileResponse response = new UserProfileResponse(10, 500L);

            //when
            when(userService.getMyProfile(email)).thenReturn(response);

            mockMvc.perform(get("/api/v1/users/me/profile")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data.level").value(10))
                    .andExpect(jsonPath("$.data.exp").value(500L))
                    .andExpect(jsonPath("$.data.totalPlaySeconds").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).getMyProfile(email);
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 프로필을 조회할 수 없다")
        void getMyProfile_returnsUnauthorizedWhenPrincipalDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/profile"))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("INF_UNITY_024: 인증된 사용자의 지갑을 반환한다")
        void getMyWallet_returnsWallet() throws Exception {
            //given
            String email = "hero@example.com";
            ShowWalletResponse response = new ShowWalletResponse(5000L, 10L);

            //when
            when(userService.getMyWallet(email)).thenReturn(response);

            mockMvc.perform(get("/api/v1/users/me/wallet")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data.gold").value(5000L))
                    .andExpect(jsonPath("$.data.gem").value(10L))
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).getMyWallet(email);
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 지갑을 조회할 수 없다")
        void getMyWallet_returnsUnauthorizedWhenPrincipalDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/wallet"))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class 로그아웃_테스트 {
        @BeforeEach
        void setUp() {
            userService = mock(UserService.class);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            mockMvc = MockMvcBuilders
                    .standaloneSetup(new UserController(userService))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("INF_UNITY_004: 서버 측 Refresh Token을 삭제하고 로그아웃한다")
        void logout_deletesRefreshTokenAndReturnsSuccess() throws Exception {
            //given
            String email = "hero@example.com";

            mockMvc.perform(post("/api/v1/auth/logout")
                            .principal(() -> email))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.USER_LOGOUT.getSuccessMessage()))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).logout(email);
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 로그아웃할 수 없다")
        void logout_returnsUnauthorizedWhenPrincipalDoesNotExist() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout"))
            //then
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 사용자를 찾을 수 없으면 Unauthorized 응답을 반환한다")
        void logout_returnsUnauthorizedWhenUserDoesNotExist() throws Exception {
            //given
            String email = "hero@example.com";

            //when
            doThrow(new BusinessException(
                    ErrorCode.UNAUTHORIZED_USER
            )).when(userService).logout(email);

            mockMvc.perform(post("/api/v1/auth/logout")
                            .principal(() -> email))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class 토큰_갱신_테스트 {
        @BeforeEach
        void setUp() {
            userService = mock(UserService.class);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();

            mockMvc = MockMvcBuilders
                    .standaloneSetup(new UserController(userService))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("Refresh Token으로 새 토큰을 발급한다")
        void refreshToken_returnsRefreshTokenRotation() throws Exception {
            //given
            RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
            RefreshTokenRotation response = new RefreshTokenRotation(
                    "new-access-token",
                    "new-refresh-token",
                    900L
            );

            //when
            when(userService.refreshToken(request)).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "refreshToken": "old-refresh-token"
                                    }
                                    """))
            //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
                    .andExpect(jsonPath("$.data.accessExpiresInSeconds").value(900L))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("Refresh Token이 유효하지 않으면 Unauthorized 응답을 반환한다")
        void refreshToken_returnsUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
            //given
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

            //when
            when(userService.refreshToken(request)).thenThrow(new BusinessException(
                    ErrorCode.UNVALID_REFRESH_TOKEN
            ));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "refreshToken": "invalid-refresh-token"
                                    }
                                    """))
            //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }
}
