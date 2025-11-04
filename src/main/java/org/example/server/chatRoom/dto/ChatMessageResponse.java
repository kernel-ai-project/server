package org.example.server.chatRoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record ChatMessageResponse(
        Long messageId,
        boolean isUser,
        Long userId,
        String message,
        @JsonProperty("created_time")
        OffsetDateTime createdTime
) {
}
