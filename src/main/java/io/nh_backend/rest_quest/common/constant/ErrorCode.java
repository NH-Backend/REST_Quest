package io.nh_backend.rest_quest.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //USER
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token 입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Access Token 입니다."),
    UNVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 Refresh Token 입니다.");

    private final HttpStatus status;
    private final String description;
}
