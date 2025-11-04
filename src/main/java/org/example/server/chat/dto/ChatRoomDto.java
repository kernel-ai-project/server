package org.example.server.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomDto {

    private Long chatRoomId;

    private Boolean isFavorited;
}
