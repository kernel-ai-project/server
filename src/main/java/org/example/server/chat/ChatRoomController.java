package org.example.server.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.dto.CreateChatRoomRequest;
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
