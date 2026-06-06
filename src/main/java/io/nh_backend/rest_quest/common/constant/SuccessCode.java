package io.nh_backend.rest_quest.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    //item
    ITEM_READ_SINGLE("아이템이 정상적으로 조회되었습니다."),
    ITEM_READ("모든 아이템이 정상적으로 조회되었습니다."),
    INVENTORY_READ("인벤토리 조회가 완료되었습니다."),
    ITEM_GET("아이템을 획득했습니다."),
    ITEM_DISCARD("아이템을 버렸습니다."),

    //USER
    USER_CREATED("가입되었습니다."),
    USER_LOGIN("로그인되었습니다.");

    private final String successMessage;
}
