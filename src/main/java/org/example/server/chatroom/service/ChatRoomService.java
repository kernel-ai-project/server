package org.example.server.chatroom.service;

import org.example.server.chatroom.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChatRoomService {

    Mono<ChatRoomResponse.CreateChatRoomResponse> createChatRoom(Long userId, CreateChatRoomRequest request);

    Mono<ChatRoomMessagesResponse> getChatMessages(Long chatRoomId);

    List<ChatRoomResponse.GetChatRoomResponse> findChatRooms(Long userId);

    void deleteChatRoom(Long userId, Long chatRoomId);

    ChatRoomDto updateFavorite(Long userId, Long chatRoomId, boolean isFavorited);

    UpdateChatRoomTitleResponseDto updateChatRoomTitle(Long userId, Long chatRoomId, UpdateChatRoomTitleRequestDto requestDto);
}