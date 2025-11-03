package org.example.server.chatRoom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(@NotBlank String question) {
}
