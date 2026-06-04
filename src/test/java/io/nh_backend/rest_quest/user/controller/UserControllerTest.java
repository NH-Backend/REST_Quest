package io.nh_backend.rest_quest.user.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.user.domain.Provider;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.Status;
import io.nh_backend.rest_quest.user.dto.LoginRequest;
import io.nh_backend.rest_quest.user.dto.LoginResponse;
import io.nh_backend.rest_quest.user.dto.UseCreateRequest;
import io.nh_backend.rest_quest.user.dto.UserResponse;
import io.nh_backend.rest_quest.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

            mockMvc.perform(post("/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "hero@example.com",
                                      "password": "password123",
                                      "nickname": "hero"
                                    }
                                    """))
            //then
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(SuccessCode.USER_CREATED.getSuccessMessage()))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.email").value(request.email()))
                    .andExpect(jsonPath("$.data.nickname").value(request.nickname()))
                    .andExpect(jsonPath("$.data.role").value(Role.USER.name()))
                    .andExpect(jsonPath("$.data.status").value(Status.ACTIVE.name()))
                    .andExpect(jsonPath("$.data.provider").value(Provider.LOCAL.name()))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("request가 올바르지 않을 때 bad Request 반환")
        void signup_returnsBadRequestWhenRequestIsInvalid() throws Exception {
            //given
            UseCreateRequest request = new UseCreateRequest(
                    "invalid-email",
                    "short",
                    "h"
            );

            //when
            mockMvc.perform(post("/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "invalid-email",
                                      "password": "short",
                                      "nickname": "h"
                                    }
                                    """))
            //then
                    .andExpect(status().isBadRequest());
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

            mockMvc.perform(post("/users/login")
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
            mockMvc.perform(post("/users/login")
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
    }
}
