package io.nh_backend.rest_quest.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    USER_CREATED("로그인 되었습니다."),

    //item
    ITEM_READ_SINGLE("아이템이 정상적으로 조회되었습니다."),
    ITEM_READ("모든 아이템이 정상적으로 조회되었습니다.");
    private final String successMessage;
}
