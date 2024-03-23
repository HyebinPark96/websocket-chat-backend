package com.websocket.chat.controller;

import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoomVO;
import com.websocket.chat.model.UserVO;
import com.websocket.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import java.util.Map;

// STOMP 는 통신규약 중 하나로, 바로 전송하지 않고, 브로커 (커맨드, 헤더, 바디 포함) 끼고 통신하는 것 ??
// @SendTo 등 스프링에서는 STOMP를 지원하고 있고, 내장 브로커를 제공한다.
@Controller
public class ChatController {

    @Autowired
    ChatService chatService;

    private boolean flagForCheckWriter;

    // 유저 채팅 입장 시 인사
    @MessageMapping("/enter") // '/app/enter' // 구독자 -> 브로커
    public /*ChatEnter*/ void enterRoom(ChatMsg chatMsg, ChatRoomVO chatRoomVO) throws Exception {
        /*return chatService.enterRoom(chatMsg, chatRoom);*/
        chatService.enterRoom(chatMsg, chatRoomVO);
    }
    
    // 유저 채팅 시
    @MessageMapping("/chat")
    public ChatMsg sendMsg(ChatMsg chatMsg, ChatRoomVO chatRoomVO) {
        return chatService.sendMsg(chatMsg, chatRoomVO);
    }

    // 유저 참여 시
    @MessageMapping("/add")
    public List<String> addUserList(ChatRoomVO chatRoomVO) {
        return chatService.getConnWriterListForAddUser(chatRoomVO);
    }

    // 유저 퇴장 시
    @MessageMapping("/minus")
    public List<String> getConnWriterListForMinusWriter(ChatRoomVO chatRoomVO) {
        return chatService.getConnWriterListForMinusWriter(chatRoomVO);
    }

    // 강퇴 시
    @MessageMapping("/expulsion")
    public List<String> sendExpulsion(ChatRoomVO chatRoomVO) {
        chatService.minusWriterForChatRoom(chatRoomVO); // 감소
        return chatService.sendExpulsion(chatRoomVO);
    }






    // 귓속말
    @MessageMapping("/whisper")
    public ChatMsg sendMsgForWhisper(ChatMsg chatMsg) {
        return chatService.sendMsgForWhisper(chatMsg);
    }
    
    // 서버의 클라이언트 연결 끊기
    @MessageMapping("/disconn")
    public ChatMsg sendDisconnect(ChatMsg chatMsg) {
        return chatService.sendDisconnect(chatMsg);
    }

    // 관리자 공지사항
    @MessageMapping("/notice")
    @SendTo("/topic/notice")
    public ChatRoomVO sendNotice(ChatRoomVO chatRoomVO) {
        return chatRoomVO;
    }

    // 관리자
    @GetMapping("/admin")
    public String adminMain() {
        return "admin";
    }

    // 채팅방 생성 OR 채팅방 목록 선택하는 화면 (아이디 함께 전송되므로 보안상 POST)
    @PostMapping("/crossroads")
    public String crossroads(ChatMsg chatMsg, Model model) {
        model.addAttribute("writer", chatMsg.getWriter());
        return "crossroads";
    }

    // 채팅방 생성 폼
    @PostMapping("/chatRoomFrm")
    public String getChatRoomFrm(ChatMsg chatMsg, Model model) {
        model.addAttribute("writer", chatMsg.getWriter());
        return "chatRoomFrm";
    }

//    // 채팅방 목록
//    @PostMapping("/chatRoomList")
//    public String getChatRoomList(ChatMsg chatMsg, Model model) {
//        Map<String, ChatRoomVO> roomMap = chatService.getChatRoomMap();
//        model.addAttribute("writer", chatMsg.getWriter());
//        model.addAttribute("roomMap", roomMap);
//        return "chatRoomList";
//    }

//    // 채팅방 입장
//    @PostMapping("/chatRoom/{chatRoomName}")
//    public String enterChatRoom(@PathVariable Long roomId, ChatMsg chatMsg, Model model) {
//        String chatRoomMaster = chatService.getChatRoomMaster(roomId);
//        model.addAttribute("chatRoomMaster", chatRoomMaster);
//        model.addAttribute("writer", chatMsg.getWriter());
//        model.addAttribute("chatRoomName", chatRoomName);
//        return "chatRoom";
//    }

    // 첫화면
    @GetMapping(value = {"/"})
    public String enterLobby() {
        return "lobby";
    }




}
