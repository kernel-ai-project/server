package org.example.server.chat;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.chat.dto.ChatRoomDto;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.entity.ChatRoom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ChatService {
    Mono<AskResponse> ask(AskRequest req);

    Flux<String> askStream(AskRequest req);

    List<ChatRoomResponse> findChatRooms(Long userId);

    void deleteChatRoom(Long userId, Long chatRoomId);

    ChatRoomDto updateFavorite(Long userId, Long chatRoomId);

    public Map<String, Long> saveMessage(Long userId, String question, Long chatRoomId,Boolean is_user);

}