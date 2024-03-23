package com.websocket.chat.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class ChatRoomVO {

    /** Key **/
    private Long roomId;

    /** 방장 **/
    private String master;

    /** 입장 또는 퇴장할 사용자 **/
    private String tempUser;

    /** 채팅방명 **/
    private String roomName;

    /** 정원 **/
    private Long headcount;

    /** 접속중인 사용자 수 **/
    private Long connUserCnt;

    /** 접속중인 사용자 List **/
    private List<String> connUserList;

}
