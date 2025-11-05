package org.example.server.chatroom.dto;

import lombok.Builder;
import lombok.Getter;


public class ChatRoomResponse {
    @Getter
    @Builder
    public static class CreateChatRoomResponse {
        Long chatRoomId;
        String title;
        String answer;
    }

    @Getter
    @Builder
    public static class GetChatRoomResponse {
        Long chatRoomId;
        String title;
        Boolean isFavorited;
    }

}