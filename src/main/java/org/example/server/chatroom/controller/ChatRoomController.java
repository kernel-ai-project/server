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
    public Mono<ResponseEntity<ChatRoomResponse.CreateChatRoomResponse>> create(@Valid @RequestBody CreateChatRoomRequest request,
                                                                                @AuthenticationPrincipal CustomOAuth2User user) {
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
    @GetMapping
    public ResponseEntity<List<ChatRoomResponse.GetChatRoomResponse>> getChatRooms(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();

        List<ChatRoomResponse.GetChatRoomResponse> chatRooms = chatRoomService.findChatRooms(userId);

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatRoom(@AuthenticationPrincipal CustomOAuth2User user,
                                                            @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        chatRoomService.deleteChatRoom(userId, chatRoomId);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("채팅방이 삭제되었습니다."));
    }

    /**
     * 채팅방 즐겨찾기 추가
     */
    @PatchMapping("/{chatRoomId}/favorite/enable")
    public ResponseEntity<ApiResponse<ChatRoomResponse.GetChatRoomFavorite>> addChatRoomFavorite(@AuthenticationPrincipal CustomOAuth2User user,
                                                                        @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        ChatRoomResponse.GetChatRoomFavorite chatRoom = chatRoomService.updateFavorite(userId, chatRoomId, true);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("성공적으로 즐겨찾기를 완료했습니다.", chatRoom));
    }

    /**
     * 채팅방 즐겨찾기 해제
     */
    @PatchMapping("/{chatRoomId}/favorite/disable")
    public ResponseEntity<ApiResponse<ChatRoomResponse.GetChatRoomFavorite>> removeChatRoomFavorite(@AuthenticationPrincipal CustomOAuth2User user,
                                                                           @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        ChatRoomResponse.GetChatRoomFavorite chatRoom = chatRoomService.updateFavorite(userId, chatRoomId, false);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("성공적으로 즐겨찾기 해제를 완료했습니다.", chatRoom));
    }

    /**
     * 채팅방 제목 수정
     */
    @PatchMapping("/{chatRoomId}/{userId}/title")
    public ResponseEntity<ApiResponse<ChatRoomResponse.GetChatRoomTitle>> editChatRoomTitle(@AuthenticationPrincipal CustomOAuth2User user,
                                                                                            @PathVariable Long chatRoomId,
                                                                                            @RequestBody ChatRoomTitleRequest request) {
        Long userId = user.getUserId();

        ChatRoomResponse.GetChatRoomTitle chatRoom = chatRoomService.updateChatRoomTitle(userId, chatRoomId, request.getTitle());

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("채팅방 이름이 성공적으로 수정되었습니다.",chatRoom));

    }
}
