package org.example.server.chat;

import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.AiChatRoomRequest;
import org.example.server.chat.dto.AiChatRoomResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FastApiChatRoomClient {

    private final WebClient fastapiClient;

    public Mono<AiChatRoomResponse> createChatRoom(String query) {
        return fastapiClient.post()
                .uri("/api/chatrooms")
                .bodyValue(new AiChatRoomRequest(query))
                .retrieve()
                .bodyToMono(AiChatRoomResponse.class);
    }
}
