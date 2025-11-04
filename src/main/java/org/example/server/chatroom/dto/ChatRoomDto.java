package org.example.server.chatroom.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomDto {

    private Long chatRoomId;

    private Boolean isFavorited;
}