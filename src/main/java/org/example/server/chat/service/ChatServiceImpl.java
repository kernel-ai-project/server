package org.example.server.chat.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.entity.Message;
import org.example.server.chatroom.entity.ChatRoom;
import org.example.server.chatroom.repository.ChatRoomRepository;
import org.example.server.chatroom.repository.MessageRepository;
import org.example.server.user.entity.User;
import org.example.server.user.respository.UserRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final WebClient fastapiClient;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatRedisService chatRedisService;


    @Override
    public Mono<AskResponse> ask(AskRequest req) {
        return fastapiClient.post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(AskResponse.class);

    }

    @Override
    public Flux<String> askStream(AskRequest req) {
        return fastapiClient.post()
                .uri("/ask/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .bodyValue(req)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(this::toUtf8String)
                .filter(chunk -> !chunk.isBlank());   // 필요시 버퍼링 로직 추가
    }

    private String toUtf8String(DataBuffer buffer) {
        // --- 👈 이 부분을 추가해야 합니다.
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return new String(bytes, StandardCharsets.UTF_8);
    }


    //todo: 사용자 질문, 에이전트 답변 저장
    @Override
    @Transactional
    public String saveMessage(Long userId, String message, Long chatRoomId, Boolean isUser) {

//        //채팅방 조회 or 생성
//        ChatRoom chatRoom = getOrCreateChatRoom(userId, message, chatRoomId);
//
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        //DB에 메시지 저장
        Message savedMessage = saveMessage(chatRoom, message, isUser);

        chatRedisService.saveMessage(chatRoom.getChatRoomId(), isUser, message);

        return savedMessage.getContent();
    }






    // === 내부 유틸 메서드 === //


    // 채팅방 조회 또는 생성
    private ChatRoom getOrCreateChatRoom(Long userId, String question, Long chatRoomId) {
        if (chatRoomId == null) {
            return createNewChatRoom(userId, question);
        }

        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId));
    }

    // 새 채팅방 생성 및 저장
    private ChatRoom createNewChatRoom(Long userId, String question) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ChatRoom newChatRoom = ChatRoom.builder()
                .user(user)
                .title(question)
                .build();

        return chatRoomRepository.save(newChatRoom);
    }

    // 메시지 생성 및 저장
    private Message saveMessage(ChatRoom chatRoom, String content, boolean isUser) {
        Message message = Message.builder()
                .chatRoom(chatRoom)
                .content(content)
                .isUser(isUser)
                .build();

        return messageRepository.save(message);
    }

}
