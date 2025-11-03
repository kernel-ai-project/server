package org.example.server.chat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.entity.ChatRoom;
import org.example.server.chat.entity.User;
import org.example.server.chat.respository.ChatRoomRepository;
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

    private final ChatRoomRepository chatRoomRepository;

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
    public List<ChatRoomResponse> findChatRooms(Long userId) {

        return chatRoomRepository.findByUserId(userId);
    }

    @Override
    public void deleteChatRoom(Long userId, Long chatRoomId) {

        if (!chatRoomRepository.existsByUser_UserIdAndChatRoomId(userId, chatRoomId)) {
            throw new IllegalArgumentException("채팅방이 존재하지 않습니다.");
        } else {
            chatRoomRepository.deleteById(chatRoomId);
        }

    }
}