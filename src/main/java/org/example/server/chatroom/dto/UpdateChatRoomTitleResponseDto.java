package org.example.server.chatroom.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateChatRoomTitleResponseDto
{
    private Long chatRoomId;
    private String title;
}
