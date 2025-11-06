package org.example.server.chatroom.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateChatRoomTitleRequestDto
{
    @NotBlank(message = "채팅방 제목은 비워둘 수 없습니다.")
    private String title;
}
