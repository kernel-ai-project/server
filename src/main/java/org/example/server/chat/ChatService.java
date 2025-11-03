package org.example.server.chat;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.chat.dto.ChatRoomResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChatService {
    Mono<AskResponse> ask(AskRequest req);

    Flux<String> askStream(AskRequest req);

    List<ChatRoomResponse> findChatRooms(Long userId);

    void deleteChatRoom(Long userId, Long chatRoomId);
}