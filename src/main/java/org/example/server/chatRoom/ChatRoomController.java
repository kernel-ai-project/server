package org.example.server.chatRoom;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.chatRoom.dto.ChatRoomResponse;
import org.example.server.chatRoom.dto.CreateChatRoomRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public Mono<ResponseEntity<ChatRoomResponse>> create(@Valid @RequestBody CreateChatRoomRequest request) {
        return chatRoomService.createChatRoom(request)
                .map(ResponseEntity::ok);
    }
}
