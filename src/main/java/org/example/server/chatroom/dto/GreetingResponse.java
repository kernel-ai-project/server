package org.example.server.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GreetingResponse {
    private boolean success;
    private GreetingResult result;
}