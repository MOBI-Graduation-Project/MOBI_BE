package com.mobi.mobi.chat;

import com.mobi.mobi.chat.dto.ChatMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChattingIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;


    //본인과 상대방의 토큰,채팅방 번호 입력
    private final String USER_A_TOKEN = "";
    private final String USER_B_TOKEN = "";
    private final Long CHAT_ROOM_ID = 1L;

    @BeforeEach
    void setUp() {

        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("채팅 통합 테스트: 사용자 A가 채팅방을 구독하고, 사용자 B가 보낸 메시지를 수신한다.")
    void chatIntegrationTest() throws Exception {

        // 메시지 수신을 기다리는 CompletableFuture
        CompletableFuture<ChatMessageDTO> messageFuture = new CompletableFuture<>();

        // 1. 사용자 A 웹소켓 연결
        StompHeaders userAHeaders = createStompHeaders(USER_A_TOKEN);
        // StompSessionHandler를 통해 연결이 '완료된 후'의 동작을 정의
        StompSession userASession = stompClient.connect(
                getUrl(),
                new WebSocketHttpHeaders(),
                userAHeaders,
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        // 연결이 성공하면, 그 때 구독을 시작
                        session.subscribe("/sub/chat/room/" + CHAT_ROOM_ID, new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return ChatMessageDTO.class;
                            }
                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                // 메시지 도착 시 Future를 완료시킴
                                messageFuture.complete((ChatMessageDTO) payload);
                            }
                        });
                    }
                }
        ).get(3, TimeUnit.SECONDS);


        // 2. 사용자 B 웹소켓 연결 (마찬가지로 연결이 완료된 것을 보장받음)
        StompHeaders userBHeaders = createStompHeaders(USER_B_TOKEN);
        StompSession userBSession = connectToWebSocket(userBHeaders);

        // 3. 사용자 B가 메시지 전송
        ChatMessageDTO messageToSend = new ChatMessageDTO();
        messageToSend.setRoomId(CHAT_ROOM_ID);
        messageToSend.setContent("안녕하세요! B입니다.");
        userBSession.send("/pub/chat/message", messageToSend);

        // 4. 사용자 A가 보낸 메시지를 5초 안에 수신하는지 확인
        ChatMessageDTO receivedMessage = messageFuture.get(5, TimeUnit.SECONDS);

        // 검증
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo("안녕하세요! B입니다.");
        assertThat(receivedMessage.getRoomId()).isEqualTo(CHAT_ROOM_ID);
        System.out.println("수신된 메시지: " + receivedMessage.getSenderNickname() + " - " + receivedMessage.getContent());
    }

    private String getUrl() {
        return "ws://localhost:" + port + "/ws";
    }

    private StompHeaders createStompHeaders(String token) {
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    private StompSession connectToWebSocket(StompHeaders stompHeaders) throws Exception {
        return stompClient.connect(
                getUrl(),
                new WebSocketHttpHeaders(),
                stompHeaders,
                new StompSessionHandlerAdapter() {}
        ).get(3, TimeUnit.SECONDS);
    }
}

