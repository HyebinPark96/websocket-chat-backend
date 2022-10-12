package com.websocket.chat.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

// WebSocketConfigurer를 구현한 WebSocketConfig 설정파일
// => WebSocketMessageBrokerConfigurer를 구현한 StompWebSocketConfig로 변경
@Configuration // 스프링 컨테이너에 Bean 등록
@EnableWebSocketMessageBroker // Stomp의 메시지브로커 사용하기 위해 선언하는 어노테이션
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer { // WebSocketMessageBrokerConfigurer : 웹 소켓 연결을 구성하기 위한 메서드를 구현하고 제공
    @Override
    // configureMessageBroker() : Simple Message Broker를 활성화
    public void configureMessageBroker(MessageBrokerRegistry registry) { // configureMessageBroker() : 한 클라이언트에서 다른 클라이언트로 메시지를 라우팅 하는 데 사용될 메시지 브로커를 구성 (스프링에서 제공해주는 내장 브로커 사용)
        // 메세지 브로커 => 구독자(해당 채팅방 구독하는 클라이언트)
        // (메시지 구독 요청) 메시지 받을 때 prefix
        
        // enableSimpleBroker() : 해당 파라미터의 주소의 구독자들에게 메시지브로커가 메시지 전달
        registry.enableSimpleBroker("/topic", "/queue"); // 이 두개의 prefix를 보면 브로커가 가로채서 구독자에게 전송 // 일반적으로 "/queue"는 1대1 메시징, "/topic"은 1대다 메시징

        // 구독자(해당 채팅방 구독하는 클라이언트) => 브로커
        // (메시지 송신 요청) 메시지 보낼 때 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트에서 서버로 WebSocket 연결을 하고 싶을 때, "/ws" 으로 요청을 보냄
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 사용 (웹소켓 지원되지 않는 일부 브라우저들 연결 지원)
    }

}
