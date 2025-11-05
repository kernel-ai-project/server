package org.example.server.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponse {

    private Long chatRoomId;

    private String title;

    private Boolean isFavorited;



}
