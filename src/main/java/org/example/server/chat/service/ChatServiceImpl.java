package org.example.server.chat.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.example.server.chat.dto.AnswerRequest;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatMessage;
import org.example.server.chat.entity.Message;
import org.example.server.chatroom.entity.ChatRoom;
import org.example.server.chatroom.repository.ChatRoomRepository;
import org.example.server.chatroom.repository.MessageRepository;
import org.example.server.user.entity.User;
import org.example.server.user.repository.UserRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
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
    public Flux<String> askStream(AnswerRequest req) {
        return fastapiClient.post()
                .uri("/ask/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .bodyValue(req)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(this::toUtf8String)
                .filter(chunk -> !chunk.isBlank());
    }

    private String toUtf8String(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Transactional
    @Override
    public void saveQuestion(Long userId, String message, Long chatRoomId, Boolean isUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // DB에 메시지 저장
        Message savedMessage = saveMessage(chatRoom, message, isUser);

        // Redis에 메시지 저장
        chatRedisService.saveMessage(chatRoom.getChatRoomId(), isUser, message);
    }

    @Override
    public Flux<String> askStreamWithContext(Long userId, Long chatRoomId, String question) {
        // 1. Redis에서 최근 대화 내역과 요약 가져오기
        List<Object> redisHistory = chatRedisService.getRecentHistory(chatRoomId);
        String summary = chatRedisService.getSummary(chatRoomId);

        // 2. Redis 형식을 FastAPI 형식으로 변환
        List<ChatMessage> history = convertToFastApiFormat(redisHistory);

        // 3. history와 summary를 포함한 AnswerRequest 생성
        AnswerRequest requestWithContext = new AnswerRequest(question, history, summary);

        // 4. FastAPI로 전달하여 스트리밍 답변 받기
        Flux<String> answerStream = askStream(requestWithContext);

        // 5. 답변 조각을 누적하면서 스트리밍
        AtomicReference<StringBuilder> fullAnswerBuilder = new AtomicReference<>(new StringBuilder());

        return answerStream
                .doOnNext(chunk -> {
                    // 각 chunk를 누적
                    fullAnswerBuilder.get().append(chunk);
                })
                .doOnComplete(() -> {
                    // 스트리밍이 완료된 후에 전체 답변 저장
                    String fullAnswer = fullAnswerBuilder.get().toString();
                    saveQuestion(userId, fullAnswer, chatRoomId, false);
                })
                .doOnError(error -> {
                    log.error("Error during streaming: ", error);
                });
        // 6. 원본 스트림을 그대로 반환 (클라이언트는 실시간으로 받음)
    }
    /**
     * Redis 형식을 FastAPI 형식으로 변환
     */
    private List<ChatMessage> convertToFastApiFormat(List<Object> redisHistory) {
        if (redisHistory == null || redisHistory.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatMessage> messages = new ArrayList<>();

        for (Object obj : redisHistory) {
            String message = obj.toString();

            if (message.startsWith("user:")) {
                messages.add(new ChatMessage("user", message.substring(5)));
            } else if (message.startsWith("assistant:")) {
                messages.add(new ChatMessage("assistant", message.substring(10)));
            } else if (message.startsWith("bot:")) {
                messages.add(new ChatMessage("assistant", message.substring(4)));
            }
        }

        return messages;
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