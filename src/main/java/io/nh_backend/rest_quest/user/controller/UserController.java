package io.nh_backend.rest_quest.user.controller;

import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.dto.ApiResponse;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PostMapping({"/api/v1/users/register"})
    public ApiResponse<UserResponse> signup(@Valid @RequestBody UseCreateRequest request) {
        return ApiResponse.ok(
                userService.createUser(request),
                SuccessCode.USER_CREATED.getSuccessMessage()
        );
    }

    @PostMapping({"/api/v1/auth/login"})
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(
                userService.login(request),
                SuccessCode.USER_LOGIN.getSuccessMessage()
        );
    }

    @PostMapping("/api/v1/auth/refresh")
    public ApiResponse<RefreshTokenRotation> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ApiResponse.ok(
                userService.refreshToken(request),
                null
        );
    }

    @PostMapping("/api/v1/auth/logout")
    public ApiResponse<Void> logout(
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        userService.logout(principal.getName());

        return ApiResponse.ok(
                null,
                SuccessCode.USER_LOGOUT.getSuccessMessage()
        );
    }

    @GetMapping({ "/api/v1/users/me/data"})
    public ApiResponse<UserDataResponse> getMyData(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                userService.getMyData(principal.getName()),
                null
        );
    }

    @GetMapping({ "/api/v1/users/me/profile"})
    public ApiResponse<UserProfileResponse> getMyProfile(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                userService.getMyProfile(principal.getName()),
                null
        );
    }

    @GetMapping({ "/api/v1/users/me/wallet"})
    public ApiResponse<ShowWalletResponse> getMyWallet(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                userService.getMyWallet(principal.getName()),
                null
        );
    }

    @GetMapping({ "/api/v1/users/me"})
    public ApiResponse<UserResponse> getMyAccount(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return ApiResponse.ok(
                userService.getMyAccount(principal.getName()),
                null
        );
    }
}
