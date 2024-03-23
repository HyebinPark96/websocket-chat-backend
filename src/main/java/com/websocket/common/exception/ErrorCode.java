package com.websocket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "1001", "채팅방이 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "1002", "유저가 존재하지 않습니다."),
    NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "1003", "이미 존재합니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1004", "내부 서버 에러입니다.");

    /** HTTP 상태 **/
    private final HttpStatus httpStatus;

    /** 코드 **/
    private final String code;

    /** 메세지 **/
    private final String message;

}
