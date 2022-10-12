package com.websocket.chat.service;

import com.websocket.chat.model.ChatEnter;
import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // writer 목록
/*    private Map<String, String> userMap = new HashMap<>(); // K : writer (chatMsg.getWriter), V : sessionId*/
    private Map<String, Integer> userMap = new HashMap<>(); // K : writer (chatMsg.getWriter), V : 임시 1

    // 채팅방 목록
    private Map<String, ChatRoom> roomMap = new HashMap<>(); // K : 채팅방 이름, V : 채팅방 커맨드 객체 (채팅방 인원, 정원 꺼내기 위함)


    private boolean flagForCheckUser;

    // 정원 내 유저 증가 가능한지 여부
    private boolean flagForAddWriter;

    // 나가기 버튼 클릭시 되었는지
    private boolean flagForMinusWriter;

    // 귓속말
    public /*ChatEnter*/ void enterRoom(ChatMsg chatMsg, ChatRoom chatRoom) { // 보내줄 구독 주소, payload(데이터)
        ChatEnter enterMsg = new ChatEnter(chatRoom.getChatRoomName() + " 방에 " + chatMsg.getWriter() + "님이 입장하셨습니다.");

        simpMessagingTemplate.convertAndSend("/topic/enter/" + chatRoom.getChatRoomName(),
                enterMsg);
/*
        return enterMsg;*/
    }

    public ChatMsg sendMsg(ChatMsg chatMsg, ChatRoom chatRoom) {
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getChatRoomName(),
                chatMsg);

        return chatMsg;
    }


    // 유저 참여하기 / 나가기 버튼 클릭시 접속중인 유저 리스트 테이블에 갱신
    public List<String> getConnWriterListForAddWriter(ChatRoom chatRoom) { // chatRoomName만 받아온 상태
        // 컨트롤러에서 리스트로 받음
        List<String> targetChatRoomUserList = new ArrayList<>();
        try {
            ChatRoom targetChatRoom = roomMap.get(chatRoom.getChatRoomName()); // 특정 ChatRoom 가져오기
            // 스크립트로 리스트로 받음
            simpMessagingTemplate.convertAndSend("/topic/add/" + targetChatRoom.getChatRoomName(), targetChatRoom.getChatRoomConnWriterList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기
            targetChatRoomUserList = roomMap.get(chatRoom.getChatRoomName()).getChatRoomConnWriterList();
        } catch(Exception e) {
            e.getStackTrace();
        }

        return targetChatRoomUserList;
    }

    // 유저 나가기 버튼 클릭
    public List<String> getConnWriterListForMinusWriter(ChatRoom chatRoom) {
        ChatRoom targetChatRoom = roomMap.get(chatRoom.getChatRoomName()); // 특정 ChatRoom 가져오기

        // 스크립트로 리스트로 받음
        simpMessagingTemplate.convertAndSend("/topic/minus/" + targetChatRoom.getChatRoomName(), targetChatRoom.getChatRoomConnWriterList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기

        // 컨트롤러에서 리스트로 받음
        List<String> targetChatRoomUserList  = roomMap.get(chatRoom.getChatRoomName()).getChatRoomConnWriterList();
        return targetChatRoomUserList;
    }

    // 방장 writer명 가져오기
    public String getChatRoomMaster(String chatRoomName) {
        ChatRoom targetChatRoom = roomMap.get(chatRoomName); // 특정 ChatRoom 가져오기
        String chatRoomMaster = targetChatRoom.getChatRoomMaster();
        return chatRoomMaster;
    }

    // 방장의 특정유저 강퇴
    public List<String> sendExpulsion(ChatRoom chatRoom) {
        System.out.println("sendExpulsion");
        ChatRoom targetChatRoom = roomMap.get(chatRoom.getChatRoomName()); // 특정 ChatRoom 가져오기

        // 강퇴당한 사람 구독주소로 접속중인 유저리스트 보내기
        simpMessagingTemplate.convertAndSend("/queue/expulsion/" + targetChatRoom.getChatRoomTempWriter(), targetChatRoom.getChatRoomConnWriterList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기

        // 강퇴 일어난 방의 모든 사람들이 구독하고 있는 주소로 접속중인 유저리스트 보내기
        simpMessagingTemplate.convertAndSend("/topic/minus/" + targetChatRoom.getChatRoomName(), targetChatRoom.getChatRoomConnWriterList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기


        // 컨트롤러에서 리스트로 받음
        List<String> targetChatRoomUserList  = roomMap.get(chatRoom.getChatRoomName()).getChatRoomConnWriterList();
        return targetChatRoomUserList;
    }

    // 귓속말
    public ChatMsg sendMsgForWhisper(ChatMsg chatMsg) {
        // 작성자를 함께 보내는 이유는 UI에서 [발신자 => 수신자] 나타내기 위함, 추후 공백으로 구분예정
        // 귓속말 수신자에게 보내기
        simpMessagingTemplate.convertAndSend("/queue/whisper/" + chatMsg.getReceiver(), chatMsg.getWriter() + " " + chatMsg.getMsg());
        // 귓속말 발신자에게도 보내기
        simpMessagingTemplate.convertAndSend("/queue/whisper/" + chatMsg.getWriter(), chatMsg.getWriter() + " " + chatMsg.getMsg());
        return chatMsg;
    }

    // 서버의 클라이언트 연결 끊기
    public ChatMsg sendDisconnect(ChatMsg chatMsg) {
        simpMessagingTemplate.convertAndSend("/queue/disconn/" + chatMsg.getWriter(), chatMsg.getWriter()); // 보낼 데이터 추후 수정예정
        return chatMsg;
    }


    // Writer 중복체크
    public boolean checkWriter(String inputWriter) {
        System.out.println("userMap : " + userMap);
        if(userMap.get(inputWriter) != null) { // key(writer)로 조회했을 때 value(세션)가 존재한다면 중복 Writer 있다는 의미
            flagForCheckUser = true;
        } else {
            flagForCheckUser = false;
            userMap.put(inputWriter, 1);
        }
        return flagForCheckUser;
    }

    // 채팅방 생성
    public ChatRoom addChatRoom(ChatRoom chatRoom) {
        if(roomMap.get(chatRoom.getChatRoomName()) == null) { // 중복된 채팅방 name 없다면 put
            roomMap.put(chatRoom.getChatRoomName(), chatRoom); // put
            System.out.println("roomMap : " + roomMap);
        }

        return chatRoom;
    }
    
    // 채팅방 목록 가져오기
    public Map<String, ChatRoom> getChatRoomMap() {
        return roomMap;
    }

    // 특정 채팅방 접속 후 참여하기 클릭 시 채팅방 인원 +1 증가
    public boolean addWriterForChatRoom(ChatRoom chatRoom) {
        System.out.println("서비스 추가 전 chatRoom.getChatRoomName() : " + chatRoom.getChatRoomName());
        System.out.println("서비스 추가 전 chatRoom.getChatRoomTempWriter() : " + chatRoom.getChatRoomTempWriter());

        ChatRoom targetChatRoom = roomMap.get(chatRoom.getChatRoomName()); // 매개변수로 받아온 특정 채팅방 커맨드 객체를 roomMap에서 꺼내기

        // 커맨드 객체에 접근하여 +1 증가
        if(targetChatRoom.getChatRoomHeadcount() > targetChatRoom.getChatRoomConnWriterCnt()) {
            targetChatRoom.setChatRoomConnWriterCnt(targetChatRoom.getChatRoomConnWriterCnt() + 1); // SETTER +1명 증가
            targetChatRoom.setChatRoomTempWriter(chatRoom.getChatRoomTempWriter());

            /* 커맨드 객체의 chatRoomConnWriterList에 접속 유저 추가하기 */
            List<String> targetChatRoomList = targetChatRoom.getChatRoomConnWriterList(); // 가져오기
            if(targetChatRoomList != null) { // 이미 존재한다면
                // 추가하려는 유저 이미 접속되어 있다면(나가기 하지않고 방 나갔거나 다른 버그들의 이유로) 다시 리스트에 담지않기
/*                for(int i=0; i<targetChatRoomList.size(); i++) {
                    if(!targetChatRoomList.get(i).equals(targetChatRoom.getChatRoomTempWriter())) {*/
                        targetChatRoomList.add(chatRoom.getChatRoomTempWriter());
                        targetChatRoom.setChatRoomConnWriterList(targetChatRoomList); // SETTER userMap 새로 들어온 writer 갱신
                        System.out.println("targetChatRoom : " + targetChatRoom);
/*                        break; // 추가하고 빠져나가기*/

/*                }*/

            } else { // null 이라면 바로 한명 추가
                List<String> chatRoomTempWriterList = new ArrayList<>();
                chatRoomTempWriterList.add(chatRoom.getChatRoomTempWriter());
                targetChatRoom.setChatRoomConnWriterList(chatRoomTempWriterList);
                System.out.println("targetChatRoom : " + targetChatRoom);
            }

            roomMap.put(targetChatRoom.getChatRoomName(), targetChatRoom); // 타겟 Map의 값 변경이 roomMap에 반영되기 위해서는 같은 key로 값 덮어씌우기

            System.out.println("roomMap : " + roomMap);
            
            flagForAddWriter = true; // 추가됐든 안됐든 일단 방입장은 가능한 상태
        } else { // 정원 초과하면 +1 안됨
            flagForAddWriter = false; // 추가와 관계없이 정원 초과인 상태
        }

        return flagForAddWriter;
    }

    // 특정 채팅방 나가기 버튼 클릭 시 채팅방 인원 -1 감소
    public boolean minusWriterForChatRoom(ChatRoom chatRoom) {
        flagForMinusWriter = false;

        System.out.println("서비스 삭제 전 chatRoom.getChatRoomName() : " + chatRoom.getChatRoomName());
        System.out.println("서비스 삭제 전 chatRoom.getChatRoomTempWriter() : " + chatRoom.getChatRoomTempWriter());

        ChatRoom targetChatRoom = roomMap.get(chatRoom.getChatRoomName()); // 매개변수로 받아온 특정 채팅방 커맨드 객체를 roomMap에서 꺼내기

        System.out.println("감소 전  : " + targetChatRoom.getChatRoomConnWriterCnt());

        if(targetChatRoom.getChatRoomConnWriterCnt() > 0) { // 당연히 현재 인원 0보다 많을 때만 -1 감소 되어야 함
            targetChatRoom.setChatRoomConnWriterCnt(targetChatRoom.getChatRoomConnWriterCnt() - 1);
            String chatRoomTempWriter = chatRoom.getChatRoomTempWriter(); // 현재 삭제할 타겟


            // 나가는 writer를 List에서 찾아내기
            for(int i=0; i<targetChatRoom.getChatRoomConnWriterList().size(); i++) {
                if(targetChatRoom.getChatRoomConnWriterList().get(i).equals(chatRoomTempWriter)) { // writer와 같은 List 인덱스 값 찾아냈다면
                    System.out.println("삭제 전 targetChatRoom.getChatRoomConnWriterList() : " + targetChatRoom.getChatRoomConnWriterList());
                    targetChatRoom.getChatRoomConnWriterList().remove(i); // 삭제
                    System.out.println("삭제 후 targetChatRoom.getChatRoomConnWriterList() : " + targetChatRoom.getChatRoomConnWriterList());
                    break; // 찾았으면 break
                }
            }

            roomMap.put(targetChatRoom.getChatRoomName(), targetChatRoom); // 타겟 Map의 값 변경이 roomMap에 반영되기 위해서는 같은 key로 값 덮어씌우기

            System.out.println("감소 후  : " + targetChatRoom.getChatRoomConnWriterCnt());
            System.out.println("roomMap : " + roomMap);

            flagForMinusWriter = true;
        }

        return flagForMinusWriter;
    }






}
