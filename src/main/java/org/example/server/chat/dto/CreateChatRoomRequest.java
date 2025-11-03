package org.example.server.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(@NotBlank String query) {
}
