package org.example.server.chatRoom.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.chatRoom.service.ChatRoomService;
import org.example.server.chatRoom.dto.ChatRoomMessagesResponse;
import org.example.server.chatRoom.dto.ChatRoomResponse;
import org.example.server.chatRoom.dto.CreateChatRoomRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Component("teamChatRoomController")
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
}
