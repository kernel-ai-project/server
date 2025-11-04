package org.example.server.chatRoom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChatRoomNotFoundException extends RuntimeException {

    public ChatRoomNotFoundException(Long chatRoomId) {
        super("Chat room not found: " + chatRoomId);
    }
}
