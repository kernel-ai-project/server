package org.example.server.juwon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자 필요
public class UpdateChatRoomTitleRequestDto
{
    private String title;
}
