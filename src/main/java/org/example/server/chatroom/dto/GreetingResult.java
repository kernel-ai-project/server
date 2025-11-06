package org.example.server.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GreetingResult {
    private String message;
    private String greeting;
}
