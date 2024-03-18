package com.websocket.chat.service;

import com.websocket.chat.model.ChatEnter;
import com.websocket.chat.model.ChatMsg;
import com.websocket.chat.model.ChatRoomVO;
import org.springframework.beans.factory.annotation.Autowired;
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

    /** 접속중인 사용자 Map (key: 사용자명, value: 세션 아이디) **/
    private Map<String, Integer> userMap = new HashMap<>();

    /** 채팅방 Map (key: 채팅방명, value: 채팅방 정보) **/
    private Map<String, ChatRoomVO> roomMap = new HashMap<>();

    // 정원 내 유저 증가 가능한지 여부
    private boolean flagForAddWriter;

    // 나가기 버튼 클릭시 되었는지
    private boolean flagForMinusWriter;

    /**
     * Method Description: 중복 사용자명 체크
     * Parameter Data: name(사용자명)
     * Return Data: Boolean
     **/
    public Boolean checkDuplicateName(String name) {
        if(userMap.get(name) != null) {
            return false;
        }
        userMap.put(name, 1);
        return true;
    }

    /**
     * Method Description: 채팅방 목록 조회
     * Return Data: Map<String, ChatRoomVO>
     **/
    public Map<String, ChatRoomVO> selectRoomList() {
        return roomMap;
    }

    /**
     * Method Description: 채팅방 목록 수 조회
     * Return Data: Long
     **/
    public Long selectRoomListCnt() {
        return Long.valueOf(roomMap.size());
    }

    /**
     * Method Description: 채팅방 생성
     * Parameter Data: ChatRoom(채팅방 객체)
     * Return Data: Boolean
     **/
    public Boolean addChatRoom(ChatRoomVO chatRoomVO) {
        if(roomMap.get(chatRoomVO.getRoomName()) == null) {
            roomMap.put(chatRoomVO.getRoomName(), chatRoomVO);
            System.out.println("*** 채팅방이 생성되었습니다. ***");
            System.out.println("roomMap: " + roomMap);
            return true;
        }
        return false;
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

    // 유저 참여하기 / 나가기 버튼 클릭시 접속중인 유저 리스트 테이블에 갱신
    public List<String> getConnWriterListForAddUser(ChatRoomVO chatRoomVO) { // chatRoomName만 받아온 상태
        // 컨트롤러에서 리스트로 받음
        List<String> targetRoomUserList = new ArrayList<>();
        try {
            ChatRoomVO targetRoom = roomMap.get(chatRoomVO.getRoomName()); // 특정 ChatRoom 가져오기
            // 스크립트로 리스트로 받음
            simpMessagingTemplate.convertAndSend("/topic/add/" + targetRoom.getRoomName(), targetRoom.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기
            targetRoomUserList = roomMap.get(chatRoomVO.getRoomName()).getConnUserList();
        } catch(Exception e) {
            e.getStackTrace();
        }

        return targetRoomUserList;
    }

    // 유저 나가기 버튼 클릭
    public List<String> getConnWriterListForMinusWriter(ChatRoomVO chatRoomVO) {
        ChatRoomVO targetChatRoomVO = roomMap.get(chatRoomVO.getRoomName()); // 특정 ChatRoom 가져오기

        // 스크립트로 리스트로 받음
        simpMessagingTemplate.convertAndSend("/topic/minus/" + targetChatRoomVO.getRoomName(), targetChatRoomVO.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기

        // 컨트롤러에서 리스트로 받음
        List<String> targetChatRoomUserList  = roomMap.get(chatRoomVO.getRoomName()).getConnUserList();
        return targetChatRoomUserList;
    }

    // 방장 writer명 가져오기
    public String getChatRoomMaster(String roomName) {
        ChatRoomVO targetChatRoomVO = roomMap.get(roomName); // 특정 ChatRoom 가져오기
        if(targetChatRoomVO != null) {
            return targetChatRoomVO.getMaster();
        }
        return null;
    }

    // 방장의 특정유저 강퇴
    public List<String> sendExpulsion(ChatRoomVO chatRoomVO) {
        System.out.println("sendExpulsion");
        ChatRoomVO targetChatRoomVO = roomMap.get(chatRoomVO.getRoomName()); // 특정 ChatRoom 가져오기

        // 강퇴당한 사람 구독주소로 접속중인 유저리스트 보내기
        simpMessagingTemplate.convertAndSend("/queue/expulsion/" + targetChatRoomVO.getTempUser(), targetChatRoomVO.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기

        // 강퇴 일어난 방의 모든 사람들이 구독하고 있는 주소로 접속중인 유저리스트 보내기
        simpMessagingTemplate.convertAndSend("/topic/minus/" + targetChatRoomVO.getRoomName(), targetChatRoomVO.getConnUserList()); // 특정 채팅방의 접속중인 writer 리스트 보내주기


        // 컨트롤러에서 리스트로 받음
        List<String> targetChatRoomUserList  = roomMap.get(chatRoomVO.getRoomName()).getConnUserList();
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
    
    // 채팅방 목록 가져오기
    public Map<String, ChatRoomVO> getChatRoomMap() {
        return roomMap;
    }

    // 특정 채팅방 접속 후 참여하기 클릭 시 채팅방 인원 +1 증가
    public boolean addWriterForChatRoom(ChatRoomVO chatRoomVO) {
        System.out.println("서비스 추가 전 chatRoom.getChatRoomName() : " + chatRoomVO.getRoomName());
        System.out.println("서비스 추가 전 chatRoom.getChatRoomTempWriter() : " + chatRoomVO.getTempUser());

        ChatRoomVO targetChatRoomVO = roomMap.get(chatRoomVO.getRoomName()); // 매개변수로 받아온 특정 채팅방 커맨드 객체를 roomMap에서 꺼내기

        // 커맨드 객체에 접근하여 +1 증가
        if(targetChatRoomVO.getHeadcount() > targetChatRoomVO.getConnUserCnt()) {
            targetChatRoomVO.setConnUserCnt(targetChatRoomVO.getConnUserCnt() + 1); // SETTER +1명 증가
            targetChatRoomVO.setTempUser(chatRoomVO.getTempUser());

            /* 커맨드 객체의 chatRoomConnWriterList에 접속 유저 추가하기 */
            List<String> targetChatRoomList = targetChatRoomVO.getConnUserList(); // 가져오기
            if(targetChatRoomList != null) { // 이미 존재한다면
                // 추가하려는 유저 이미 접속되어 있다면(나가기 하지않고 방 나갔거나 다른 버그들의 이유로) 다시 리스트에 담지않기
/*                for(int i=0; i<targetChatRoomList.size(); i++) {
                    if(!targetChatRoomList.get(i).equals(targetChatRoom.getChatRoomTempWriter())) {*/
                        targetChatRoomList.add(chatRoomVO.getTempUser());
                        targetChatRoomVO.setConnUserList(targetChatRoomList); // SETTER userMap 새로 들어온 writer 갱신
                        System.out.println("targetChatRoom : " + targetChatRoomVO);
/*                        break; // 추가하고 빠져나가기*/

/*                }*/

            } else { // null 이라면 바로 한명 추가
                List<String> chatRoomTempWriterList = new ArrayList<>();
                chatRoomTempWriterList.add(chatRoomVO.getTempUser());
                targetChatRoomVO.setConnUserList(chatRoomTempWriterList);
                System.out.println("targetChatRoom : " + targetChatRoomVO);
            }

            roomMap.put(targetChatRoomVO.getRoomName(), targetChatRoomVO); // 타겟 Map의 값 변경이 roomMap에 반영되기 위해서는 같은 key로 값 덮어씌우기

            System.out.println("roomMap : " + roomMap);
            
            flagForAddWriter = true; // 추가됐든 안됐든 일단 방입장은 가능한 상태
        } else { // 정원 초과하면 +1 안됨
            flagForAddWriter = false; // 추가와 관계없이 정원 초과인 상태
        }

        return flagForAddWriter;
    }

    // 특정 채팅방 나가기 버튼 클릭 시 채팅방 인원 -1 감소
    public boolean minusWriterForChatRoom(ChatRoomVO chatRoomVO) {
        flagForMinusWriter = false;

        System.out.println("서비스 삭제 전 chatRoom.getChatRoomName() : " + chatRoomVO.getRoomName());
        System.out.println("서비스 삭제 전 chatRoom.getChatRoomTempWriter() : " + chatRoomVO.getTempUser());

        ChatRoomVO targetChatRoomVO = roomMap.get(chatRoomVO.getRoomName()); // 매개변수로 받아온 특정 채팅방 커맨드 객체를 roomMap에서 꺼내기

        System.out.println("감소 전  : " + targetChatRoomVO.getConnUserCnt());

        if(targetChatRoomVO.getConnUserCnt() > 0) { // 당연히 현재 인원 0보다 많을 때만 -1 감소 되어야 함
            targetChatRoomVO.setConnUserCnt(targetChatRoomVO.getConnUserCnt() - 1);
            String chatRoomTempWriter = chatRoomVO.getTempUser(); // 현재 삭제할 타겟


            // 나가는 writer를 List에서 찾아내기
            for(int i = 0; i< targetChatRoomVO.getConnUserList().size(); i++) {
                if(targetChatRoomVO.getConnUserList().get(i).equals(chatRoomTempWriter)) { // writer와 같은 List 인덱스 값 찾아냈다면
                    System.out.println("삭제 전 targetChatRoom.getChatRoomConnWriterList() : " + targetChatRoomVO.getConnUserList());
                    targetChatRoomVO.getConnUserList().remove(i); // 삭제
                    System.out.println("삭제 후 targetChatRoom.getChatRoomConnWriterList() : " + targetChatRoomVO.getConnUserList());
                    break; // 찾았으면 break
                }
            }

            roomMap.put(targetChatRoomVO.getRoomName(), targetChatRoomVO); // 타겟 Map의 값 변경이 roomMap에 반영되기 위해서는 같은 key로 값 덮어씌우기

            System.out.println("감소 후  : " + targetChatRoomVO.getConnUserCnt());
            System.out.println("roomMap : " + roomMap);

            flagForMinusWriter = true;
        }

        return flagForMinusWriter;
    }






}
