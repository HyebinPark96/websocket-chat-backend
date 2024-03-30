package com.websocket.chat.service;

import com.websocket.chat.model.ChatEnter;
import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoomVO;
import com.websocket.common.exception.CustomException;
import com.websocket.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 사용자 List
     **/
    private List<String> userList = new ArrayList<>();

    /**
     * 채팅방 List
     **/
    private List<ChatRoomVO> roomList = new ArrayList<>();

    // 정원 내 유저 증가 가능한지 여부
    private boolean flagForAddWriter;

    // 나가기 버튼 클릭시 되었는지
    private boolean flagForMinusWriter;

    /**
     * Method Description: (공통) 방 찾기
     * Parameter Data: ChatRoomVO - 조회할 방 정보
     * Return Data: ChatRoomVO - 조회된 방 정보
     **/
    public ChatRoomVO getTargetRoom(ChatRoomVO chatRoomVO) {
        for (int i = 0; i < roomList.size(); i++) {
            if (chatRoomVO.getRoomId() == roomList.get(i).getRoomId()) {
                return roomList.get(i);
            }
        }
        throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
    }

    /**
     * Method Description: (공통) 사용자 찾기
     * Parameter Data: String - 조회할 사용자명
     * Return Data: String - 조회된 사용자명
     **/
    public String getTargetUser(String name) {
        for (int i = 0; i < userList.size(); i++) {
            if (name == userList.get(i)) {
                return userList.get(i);
            }
        }
        return null;
    }

    /**
     * Method Description: 중복 사용자명 체크
     * Parameter Data: UserVO - 조회할 사용자 정보
     * Return Data: Boolean - 중복 유무
     **/
    public Boolean checkDuplicateName(String name) {
        String targetUser = getTargetUser(name);
        if(targetUser != null) { // 유저명이 중복될 때
            return false;
        }
        return true;
    }

    /**
     * Method Description: 채팅방 목록 조회
     * Return Data: List<ChatRoomVO>
     **/
    public List<ChatRoomVO> selectRoomList() {
        return roomList;
    }

    /**
     * Method Description: 채팅방 목록 수 조회
     * Return Data: int
     **/
    public int selectRoomListCnt() {
        return roomList.size();
    }

    /**
     * Method Description: 채팅방 생성
     * Parameter Data: ChatRoom(채팅방 객체)
     * Return Data: Boolean
     **/
    public Boolean addChatRoom(ChatRoomVO chatRoomVO) {
        roomList.add(chatRoomVO);
        chatRoomVO.setRoomId((long)(roomList.size()));
        System.out.println("*** 채팅방이 생성되었습니다. ***");
        return true;
    }

    // 귓속말
    public void enterRoom(ChatMsg chatMsg, ChatRoomVO chatRoomVO) { // 보내줄 구독 주소, payload(데이터)
        ChatEnter enterMsg = new ChatEnter(chatRoomVO.getRoomName() + " 방에 " + chatMsg.getWriter() + "님이 입장하셨습니다.");

        simpMessagingTemplate.convertAndSend("/topic/enter/" + chatRoomVO.getRoomName(),
                enterMsg);
    }

    public ChatMsg sendMsg(ChatMsg chatMsg, ChatRoomVO chatRoomVO) {
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatRoomVO.getRoomName(),
                chatMsg);

        return chatMsg;
    }

    // 유저 참여 시 접속중인 유저 리스트 갱신
    public List<String> getConnWriterListForAddUser(ChatRoomVO chatRoomVO) {
        // 유저 리스트 갱신할 방 찾기
        ChatRoomVO targetRoom = getTargetRoom(chatRoomVO);

        if (targetRoom != null) {
            simpMessagingTemplate.convertAndSend("/topic/add/" + targetRoom.getRoomId(), targetRoom.getConnUserList());
            return targetRoom.getConnUserList();
        }

        throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
    }

    // 유저 퇴장 시
    public List<String> getConnWriterListForMinusWriter(ChatRoomVO chatRoomVO) {
        ChatRoomVO targetRoom = getTargetRoom(chatRoomVO);
        if (targetRoom != null) {
            simpMessagingTemplate.convertAndSend("/topic/minus/" + targetRoom.getRoomId(), targetRoom.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기
            return targetRoom.getConnUserList();
        }

        throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
    }

    // 방장 찾기
    public String getChatRoomMaster(ChatRoomVO chatRoomVO) {
        ChatRoomVO targetRoom = getTargetRoom(chatRoomVO);
        if (targetRoom != null) {
            return targetRoom.getMaster();
        }

        throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
    }

    // 강퇴 시
    public List<String> sendExpulsion(ChatRoomVO chatRoomVO) {
        ChatRoomVO targetRoom = getTargetRoom(chatRoomVO);

        if (targetRoom != null) {
            // 강퇴당한 사람 구독주소로 접속중인 유저리스트 보내기
            simpMessagingTemplate.convertAndSend("/queue/expulsion/" + targetRoom.getTempUser(), targetRoom.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기

            // 강퇴 일어난 방의 모든 사람들이 구독하고 있는 주소로 접속중인 유저리스트 보내기
            simpMessagingTemplate.convertAndSend("/topic/minus/" + targetRoom.getRoomName(), targetRoom.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기

            return targetRoom.getConnUserList();
        } else {
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        }
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

    // 참여 시 인원 +1 증가
    public Boolean addWriterForChatRoom(ChatRoomVO chatRoomVO) {
        ChatRoomVO targetRoom = getTargetRoom(chatRoomVO);

        if (targetRoom != null) {
            if (targetRoom.getHeadcount() > targetRoom.getConnUserCnt()) {
                targetRoom.setConnUserCnt(targetRoom.getConnUserCnt() + 1);

                for(int i = 0; i < targetRoom.getConnUserList().size(); i++) {
                    if(!targetRoom.getConnUserList().get(i).equals(chatRoomVO.getTempUser())) {
                        targetRoom.getConnUserList().add(chatRoomVO.getTempUser());
                    }
                }
                return true;
            } else { // 정원 초과 시
                 return false;
            }
        }

        throw new NullPointerException();
    }

    // 특정 채팅방 나가기 버튼 클릭 시 채팅방 인원 -1 감소
    public Boolean minusWriterForChatRoom(ChatRoomVO chatRoomVO) {
        ChatRoomVO targetRoom = getTargetRoom(chatRoomVO);

        if (targetRoom.getConnUserCnt() > 0) {
            targetRoom.setConnUserCnt(targetRoom.getConnUserCnt() - 1);
            String tempUser = chatRoomVO.getTempUser(); // 현재 삭제할 타겟

            // 나가는 writer를 List에서 찾아내기
            for (int i = 0; i < targetRoom.getConnUserList().size(); i++) {
                if (targetRoom.getConnUserList().get(i).equals(tempUser)) {
                    targetRoom.getConnUserList().remove(i); // 삭제
                    return true;
                }
            }

            return false;
        }

        throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
    }

}
