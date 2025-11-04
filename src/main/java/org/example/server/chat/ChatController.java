package org.example.server.chat;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatRoomDto;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.dto.ApiResponse;
import org.example.server.social.dto.CustomOAuth2User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api") // 외부에 노출되는 단일 엔드포인트
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AskResponse> ask(@RequestBody AskRequest req) {
        return chatService.ask(req);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askStream(@RequestBody AskRequest req) {
        return chatService.askStream(req);
    }

    /**
     * 채팅방 조회
     */
    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();

        List<ChatRoomResponse> chatRooms = chatService.findChatRooms(userId);

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/chatRooms/{chatRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChatRoom(@AuthenticationPrincipal CustomOAuth2User user,
                                                            @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        chatService.deleteChatRoom(userId, chatRoomId);

        return ResponseEntity.ok().body(ApiResponse.success("채팅방이 삭제되었습니다."));
    }

    /**
     * 채팅방 즐겨찾기 추가
     */
    @PatchMapping("/chatRooms/{chatRoomId}/favorite/enable")
    public ResponseEntity<ApiResponse<ChatRoomDto>> addChatRoomFavorite(@AuthenticationPrincipal CustomOAuth2User user,
                                                                        @PathVariable Long chatRoomId) {
        Long userId = user.getUserId();

        ChatRoomDto chatRoom = chatService.updateFavorite(userId, chatRoomId, true);

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

        ChatRoomDto chatRoom = chatService.updateFavorite(userId, chatRoomId, false);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("성공적으로 즐겨찾기 해제를 완료했습니다.", chatRoom));
    }
}