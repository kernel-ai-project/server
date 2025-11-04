package org.example.server.chatroom.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.chatroom.dto.*;
import org.example.server.chatroom.dto.ChatRoomResponse;
import org.example.server.chatroom.service.ChatRoomService;
import org.example.server.common.ApiResponse;
import org.example.server.social.dto.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/chatRooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public Mono<ResponseEntity<ChatRoomResponse>> create(@Valid @RequestBody CreateChatRoomRequest request) {
        return chatRoomService.createChatRoom(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{chatRoomId}/messages")
    public Mono<ResponseEntity<ChatRoomMessagesResponse>> getMessages(@PathVariable Long chatRoomId) {
        return chatRoomService.getChatMessages(chatRoomId)
                .map(ResponseEntity::ok);
    }

    /**
     * 채팅방 조회
     */
    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();

        List<ChatRoomResponse> chatRooms = chatRoomService.findChatRooms(userId);

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/chatRooms/{chatRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatRoom(@AuthenticationPrincipal CustomOAuth2User user,
                                                            @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        chatRoomService.deleteChatRoom(userId, chatRoomId);

        return ResponseEntity.ok().body(ApiResponse.success("채팅방이 삭제되었습니다."));
    }

    /**
     * 채팅방 즐겨찾기 추가
     */
    @PatchMapping("/chatRooms/{chatRoomId}/favorite/enable")
    public ResponseEntity<ApiResponse<ChatRoomDto>> addChatRoomFavorite(@AuthenticationPrincipal CustomOAuth2User user,
                                                                        @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        ChatRoomDto chatRoom = chatRoomService.updateFavorite(userId, chatRoomId, true);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("성공적으로 즐겨찾기를 완료했습니다.", chatRoom));
    }

    /**
     * 채팅방 즐겨찾기 해제
     */
    @PatchMapping("/chatRooms/{chatRoomId}/favorite/disable")
    public ResponseEntity<ApiResponse<ChatRoomDto>> removeChatRoomFavorite(@AuthenticationPrincipal CustomOAuth2User user,
                                                                           @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        ChatRoomDto chatRoom = chatRoomService.updateFavorite(userId, chatRoomId, false);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("성공적으로 즐겨찾기 해제를 완료했습니다.", chatRoom));
    }
}
