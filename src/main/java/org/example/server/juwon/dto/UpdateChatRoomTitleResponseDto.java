package org.example.server.juwon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateChatRoomTitleResponseDto {
    private String message;
    private Long chatRoomId;
    private String title;
}