package org.example.server.juwon.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.juwon.common.dto.ApiResponse;
import org.example.server.juwon.dto.UpdateChatRoomTitleRequestDto;
import org.example.server.juwon.dto.UpdateChatRoomTitleResponseDto;
import org.example.server.juwon.service.ChatRoomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms")
public class ChatRoomController
{

    private final ChatRoomService chatRoomService;

    @PatchMapping("/{chatRoomId}/title")
    public ApiResponse<UpdateChatRoomTitleResponseDto> updateChatRoomTitle(
            @PathVariable Long chatRoomId,
            @RequestBody UpdateChatRoomTitleRequestDto requestDto) { // @RequestBody로 JSON 본문을 받음

        UpdateChatRoomTitleResponseDto result = chatRoomService.updateChatRoomTitle(chatRoomId, requestDto);

        return ApiResponse.success(result);
    }

}
