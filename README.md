# 📌 Spring Boot + WebSocket 채팅 프로그램 구현  
### Java Version 8, Gradle / Spring Boot 
### stomp-websocket & sockjs 
## ✅ 기능
  * 1:N 통신
  * 일반 채팅 및 귓속말 
    * /w {id} 입력 시 귓속말 모드
    * /q 입력 시 귓속말 모드 종료
  * 사용자 관리 (구현중)
    * 사용자 정보 확인
    * 강제 연결 종료
  * 채팅방 생성
    * 채팅방 이름 및 인원 수 제한
  * 방장의 강제 퇴장 권한 및 역임 제도
## ✅ 피드백
  * Thread-safe  
    * 일반적으로 Java의 List나 Map은 파이썬과 달리 Thread-safe 하지 않다.  
  * 무분별한 send()는 비효율적  
  * UI  
    * 증가/감소 버튼 가까이 두는 것이 사용자 친화적인 UI  
  * 예외 처리
    * 에러/예외 구분
    * 예측할 수 있는 예외는 제대로 잡아서 처리한다.
