package io.nh_backend.rest_quest.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    //USER
    USER_CREATED("가입되었습니다."),
    USER_LOGIN("로그인되었습니다.");

    private final String successMessage;
}
