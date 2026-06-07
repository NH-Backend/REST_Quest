package io.nh_backend.rest_quest.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //REFRESH TOKEN
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Access Token입니다."),
    UNVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 Refresh Token입니다."),

    //User
    INVALID_LOGIN_INFORMATION(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNVALID_EMAIL_ADDRESS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED,"인증이 필요합니다."),
    PASSWORD_BAD_REQUEST(HttpStatus.BAD_REQUEST,"비밀번호는 8~64자여야 합니다"),

    //FRIEND
    FRIEND_REQUEST_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    FRIEND_RELATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 친구 관계입니다. / 이미 보낸 친구 요청이 있습니다."),
    FRIEND_REQUEST_RECEIVER_ONLY(HttpStatus.BAD_REQUEST, "본인에게 온 요청만 수락할 수 있습니다."),
    FRIEND_REQUEST_DECLINE_RECEIVER_ONLY(HttpStatus.BAD_REQUEST, "본인에게 온 요청만 거절할 수 있습니다."),
    FRIEND_REQUEST_SENDER_ONLY(HttpStatus.BAD_REQUEST, "본인이 보낸 요청만 취소할 수 있습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다."),
    FRIEND_RELATION_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 관계가 아닙니다."),

    //ITEM
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."),
    ITEM_STOCK_SHORTAGE(HttpStatus.BAD_REQUEST, "아이템 수량이 부족합니다."),
    QUANTITY_UNDER_ONE(HttpStatus.BAD_REQUEST, "지급 수량은 1개 이상이어야 합니다."),

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 요청 파라미터입니다."),

    //common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버오류가 발생했습니다.");

    private final HttpStatus status;
    private final String description;
}
