package com.websocket.chat.controller;

import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoom;
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


    // 유저 채팅 입장시 인사
    @MessageMapping("/enter") // '/app/enter' // 구독자 -> 브로커
    public /*ChatEnter*/ void enterRoom(ChatMsg chatMsg, ChatRoom chatRoom) throws Exception {
        /*return chatService.enterRoom(chatMsg, chatRoom);*/
        chatService.enterRoom(chatMsg, chatRoom);
    }
    
    // 유저 채팅
    @MessageMapping("/chat")
    public ChatMsg sendMsg(ChatMsg chatMsg, ChatRoom chatRoom) {
        return chatService.sendMsg(chatMsg, chatRoom);
    }

    // 유저 참여하기 버튼 클릭
    @MessageMapping("/add")
    public List<String> addUserList(ChatRoom chatRoom) { // chatRoomName만 받아온 상태
        return chatService.getConnWriterListForAddWriter(chatRoom); // return 접속중인 writer 리스트
    }

    // 유저 나가기 버튼 클릭
    @MessageMapping("/minus")
    public List<String> getConnWriterListForMinusWriter(ChatRoom chatRoom) {
        return chatService.getConnWriterListForMinusWriter(chatRoom);
    }

    // 방장의 특정유저 강퇴
    @MessageMapping("/expulsion")
    public List<String> sendExpulsion(ChatRoom chatRoom) {
        chatService.minusWriterForChatRoom(chatRoom); // 감소
        return chatService.sendExpulsion(chatRoom);
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
    public ChatRoom sendNotice(ChatRoom chatRoom) {
        return chatRoom;
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

    // 채팅방 목록
    @PostMapping("/chatRoomList")
    public String getChatRoomList(ChatMsg chatMsg, Model model) {
        Map<String, ChatRoom> roomMap = chatService.getChatRoomMap();
        model.addAttribute("writer", chatMsg.getWriter());
        model.addAttribute("roomMap", roomMap);
        return "chatRoomList";
    }

    // 채팅방 입장
    @PostMapping("/chatRoom/{chatRoomName}")
    public String enterChatRoom(@PathVariable String chatRoomName, ChatMsg chatMsg, Model model) {
        String chatRoomMaster = chatService.getChatRoomMaster(chatRoomName);
        model.addAttribute("chatRoomMaster", chatRoomMaster);
        model.addAttribute("writer", chatMsg.getWriter());
        model.addAttribute("chatRoomName", chatRoomName);
        return "chatRoom";
    }

    // 첫화면
    @GetMapping(value = {"/"})
    public String enterLobby() {
        return "lobby";
    }




}
