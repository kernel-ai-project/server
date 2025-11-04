package org.example.server.chat.service;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {
    Mono<AskResponse> ask(AskRequest req);

    Flux<String> askStream(AskRequest req);
}
