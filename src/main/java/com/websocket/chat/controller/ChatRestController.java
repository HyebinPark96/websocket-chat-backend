package com.websocket.chat.controller;

import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoom;
import com.websocket.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class ChatRestController {

    @Autowired
    ChatService chatService;

    private boolean flagForCheckWriter;


    // !! 하드코딩 줄일 방법 생각해보기
    // Writer 중복체크
    @PostMapping("/checkWriter")
    public boolean postCheckWriter(@RequestBody String inputWriterForCheckDupJson) {
        try{
            String replaceStr = inputWriterForCheckDupJson.replaceAll("[{:}]", " "); // 특수문자 제거

            replaceStr = replaceStr.replace("\"", ""); // 쌍따옴표 제거

            String inputWriterArr[] = new String[replaceStr.split(" ").length];
            for(int i=0; i<replaceStr.split(" ").length; i++) {
                inputWriterArr[i] = replaceStr.split(" ")[i];
            }

            String inputWriter = inputWriterArr[2]; // writer 추출

            System.out.println("inputWriter : " + inputWriter);

            if(!chatService.checkWriter(inputWriter)) { // writer 중복X
                flagForCheckWriter = false;
            } else { // writer 중복O
                flagForCheckWriter = true;
            }
            return flagForCheckWriter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 채팅방 생성
    @PostMapping("/chatRoom")
    public ChatRoom addChatRoom(@RequestBody ChatRoom chatRoom) {
        System.out.println("chatRoom : " + chatRoom);
        return chatService.addChatRoom(chatRoom); // ChatRoom 커맨드 객체 리턴
    }

    // 특정 채팅방 접속 후 Writer 입력하면 채팅방 인원 +1 증가
    @PutMapping("/chatRoom/plusConnWriterCnt")
    public boolean addWriterForChatRoom(@RequestBody ChatRoom chatRoom) {
        return chatService.addWriterForChatRoom(chatRoom);
    }

    // 특정 채팅방 접속 후 나가기 클릭 시 채팅방 인원 -1 줄이기
    @PutMapping("/chatRoom/minusConnWriterCnt")
    public boolean minusWriterForChatRoom(@RequestBody ChatRoom chatRoom) {
        return chatService.minusWriterForChatRoom(chatRoom);
    }

    // 채팅방 목록 OR 채팅방 생성 선택 페이지로 이동하면서 서버에 해당 writer name 전달하기
    @PostMapping("/moveCrossroads")
    public String moveCrossroads (@RequestBody ChatMsg chatMsg) {
        return chatMsg.getWriter(); // writer 리턴
    }


}
