
package com.websocket.chat.model;

import lombok.*;

@Data
public class ChatMsg {
    private String writer; // 발신자
    private String msg; // 메세지
    private String receiver; // 수신자 (귓속말의 경우)
    private String sessionId; // 세션ID
}

