package org.example.server.chatRoom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChatRoomAccessDeniedException extends RuntimeException {

    public ChatRoomAccessDeniedException(Long chatRoomId) {
        super("Access to chat room is denied: " + chatRoomId);
    }
}
