package org.example.server.chat;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatRoomDto;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
     * @param userId
     * @return
     */
    @GetMapping("/chatRooms/{userId}")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(@PathVariable Long userId) {
        List<ChatRoomResponse> chatRooms = chatService.findChatRooms(userId);

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 체팅방 삭제
     * @param userId
     * @param chatRoomId
     * @return
     */
    @DeleteMapping("/chatRooms/{userId}/{chatRoomId}")
    public ResponseEntity<ApiResponse> deleteChatRoom(@PathVariable Long userId,
                                                      @PathVariable Long chatRoomId) {
        chatService.deleteChatRoom(userId, chatRoomId);

        return ResponseEntity.ok().body(ApiResponse.success("채팅방이 삭제되었습니다."));
    }

    @PatchMapping("/chatRooms/{userId}/{chatRoomId}/favorite")
    public ResponseEntity<ApiResponse<ChatRoomDto>> addChatRoomFavorite(@PathVariable Long userId,
                                                           @PathVariable Long chatRoomId) {
        ChatRoomDto chatRoom = chatService.updateFavorite(userId, chatRoomId);

        return ResponseEntity.ok().body(ApiResponse.success("성공적으로 즐겨찾기를 완료했습니다.", chatRoom));
    }
}