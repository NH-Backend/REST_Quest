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
    GACHA_PURCHASED("구매가 완료되었습니다."),

    //USER
    USER_CREATED("가입되었습니다."),
    USER_LOGIN("로그인되었습니다."),
    USER_LOGOUT("로그아웃되었습니다."),

    //FRIEND
    FRIEND_REQUEST_SENT("친구 요청을 보냈습니다."),
    FRIEND_REQUEST_ACCEPTED("친구 요청을 수락했습니다."),
    FRIEND_REQUEST_DECLINED("친구 요청을 거절했습니다."),
    FRIEND_REQUEST_CANCELED("친구 요청을 취소했습니다."),
    FRIEND_DELETED("친구를 삭제했습니다.");

    private final String successMessage;
}
