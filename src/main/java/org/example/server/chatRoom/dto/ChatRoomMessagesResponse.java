package org.example.server.chatRoom.dto;

import java.util.List;

public record ChatRoomMessagesResponse(
        Long chatRoomId,
        String title,
        List<ChatMessageResponse> messages
) {
}
