package org.example.server.chatroom.dto;

import lombok.Builder;

@Builder
public record ChatRoomResponse(
        Long chatRoomId,
        String title,
        String answer
) {
}
