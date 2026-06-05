package io.nh_backend.rest_quest.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    //item
    ITEM_READ_SINGLE("아이템이 정상적으로 조회되었습니다."),
    ITEM_READ("모든 아이템이 정상적으로 조회되었습니다."),

    //USER
    USER_CREATED("가입되었습니다."),
    USER_LOGIN("로그인되었습니다."),
    USER_LOGOUT("로그아웃되었습니다.");

    private final String successMessage;
}
