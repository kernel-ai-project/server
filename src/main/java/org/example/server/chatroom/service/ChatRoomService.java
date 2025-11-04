package org.example.server.chatroom.service;

import org.example.server.chatroom.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChatRoomService {

    Mono<ChatRoomResponse> createChatRoom(CreateChatRoomRequest request);

    Mono<ChatRoomMessagesResponse> getChatMessages(Long chatRoomId);

    List<ChatRoomResponse> findChatRooms(Long userId);

    void deleteChatRoom(Long userId, Long chatRoomId);

    ChatRoomDto updateFavorite(Long userId, Long chatRoomId, boolean isFavorited);

}