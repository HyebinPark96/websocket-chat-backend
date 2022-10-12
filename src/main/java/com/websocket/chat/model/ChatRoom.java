package com.websocket.chat.model;


import lombok.*;

import java.util.List;

@Data
public class ChatRoom {
    private String chatRoomMaster; // 채팅방 방장
    private String chatRoomTempWriter; // 접속 유저 임시로 받아서 add 시킴
    private String chatRoomName; // 채팅방 이름
    private int chatRoomHeadcount; // 정원
    private int chatRoomConnWriterCnt; // 접속중인 유저 수 (최대 정원수까지 가능)
    private List<String> chatRoomConnWriterList; // 접속중인 유저 map (k: writer 이름, v: 세션아이디?)
}
