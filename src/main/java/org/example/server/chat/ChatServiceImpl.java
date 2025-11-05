package org.example.server.chat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatRoomDto;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.entity.ChatRoom;
import org.example.server.chat.entity.Message;
import org.example.server.chat.entity.User;
import org.example.server.chat.respository.ChatRoomRepository;
import org.example.server.chat.respository.UserRepository;
import org.example.server.chatRoom.repository.MessageRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
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

    @Override
    @Transactional
    public List<ChatRoomResponse> findChatRooms(Long userId) {

        return chatRoomRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteChatRoom(Long userId, Long chatRoomId) {

        if (!chatRoomRepository.existsByUser_UserIdAndChatRoomId(userId, chatRoomId)) {
            throw new IllegalArgumentException("채팅방이 존재하지 않습니다.");
        } else {
            chatRoomRepository.deleteById(chatRoomId);
        }

    }

    @Override
    @Transactional
    public ChatRoomDto updateFavorite(Long userId, Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));
        if (!chatRoom.getIsFavorited()) {
            chatRoom.addFavorite();

            return ChatRoomDto.builder()
                    .chatRoomId(chatRoom.getChatRoomId())
                    .isFavorited(chatRoom.getIsFavorited())
                    .build();
        } else {
            throw new IllegalStateException("이미 즐겨찾기된 채팅방입니다.");
        }

    }

    //todo: 사용자 질문, 에이전트 답변 저장
    @Override
    @Transactional
    public Map<String, Long> saveMessage(Long userId, String question, Long chatRoomId,Boolean is_user) {

        ChatRoom chatRoom = getOrCreateChatRoom(userId, question, chatRoomId);
        Message savedMessage = saveMessage(chatRoom, question, is_user);

        return Map.of(
                "messageId", savedMessage.getMessageId(),
                "chatRoomId", chatRoom.getChatRoomId()
        );
    }


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
