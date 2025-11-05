package org.example.server.chat.service;

import jakarta.transaction.Transactional;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ChatService {
    Mono<AskResponse> ask(AskRequest req);

    Flux<String> askStream(AskRequest req);

    String saveMessage(Long userId, String question, Long chatRoomId, Boolean is_user);

}
