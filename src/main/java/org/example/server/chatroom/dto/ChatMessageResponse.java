package org.example.server.chatroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ChatMessageResponse(
        Long messageId,
        boolean isUser,
        Long userId,
        String message,
        LocalDateTime createdTime
) {
}