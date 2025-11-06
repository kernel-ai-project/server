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
        private Long chatRoomId;
        private String title;
        private Boolean isFavorited;
    }

    @Getter
    @Builder
    public static class GetChatRoomTitle {
        private Long chatRoomId;
        private String title;
    }

    @Getter
    @Builder
    public static class GetChatRoomFavorite {
        private Long chatRoomId;
        private Boolean isFavorited;
    }

}