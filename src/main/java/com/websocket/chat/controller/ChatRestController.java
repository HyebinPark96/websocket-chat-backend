package com.websocket.chat.controller;

import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoomVO;
import com.websocket.chat.model.ResponseDataVO;
import com.websocket.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatRestController {

    @Autowired
    ChatService chatService;

    private boolean flagForCheckWriter;

    /**
     * Method Description: 중복 사용자명 체크
     * Parameter Data: 사용자명
     * Return Data: Boolean Type의 응답 객체
     **/
    @GetMapping("/users/check-username/{name}")
    public ResponseDataVO<Boolean> checkDuplicateName(@PathVariable String name) {
        ResponseDataVO<Boolean> data;
        data = new ResponseDataVO<>(chatService.checkDuplicateName(name));
        return data;
    }

    /**
     * Method Description: 채팅방 목록 조회
     * Return Data: Boolean Type의 응답 객체
     **/
    @GetMapping("/roomList")
    public List<ChatRoomVO> selectRoomList () {
        return new ArrayList<>(chatService.selectRoomList());
    }

    // 채팅방 생성
    @PostMapping("/chatRoom")
    public ResponseDataVO<Boolean> addChatRoom(@RequestBody ChatRoomVO chatRoomVO) {
        ResponseDataVO<Boolean> data;
        data = new ResponseDataVO<>(chatService.addChatRoom(chatRoomVO));
        return data; // ChatRoom 커맨드 객체 리턴
    }

    // 특정 채팅방 접속 후 Writer 입력하면 채팅방 인원 +1 증가
    @PutMapping("/chatRoom/plusConnWriterCnt")
    public boolean addWriterForChatRoom(@RequestBody ChatRoomVO chatRoomVO) {
        return chatService.addWriterForChatRoom(chatRoomVO);
    }

    // 특정 채팅방 접속 후 나가기 클릭 시 채팅방 인원 -1 줄이기
    @PutMapping("/chatRoom/minusConnWriterCnt")
    public boolean minusWriterForChatRoom(@RequestBody ChatRoomVO chatRoomVO) {
        return chatService.minusWriterForChatRoom(chatRoomVO);
    }

    // 채팅방 목록 OR 채팅방 생성 선택 페이지로 이동하면서 서버에 해당 writer name 전달하기
    @PostMapping("/moveCrossroads")
    public String moveCrossroads (@RequestBody ChatMsg chatMsg) {
        return chatMsg.getWriter(); // writer 리턴
    }


}
