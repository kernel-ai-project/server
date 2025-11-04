package org.example.server.chatroom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(@NotBlank String question) {
}