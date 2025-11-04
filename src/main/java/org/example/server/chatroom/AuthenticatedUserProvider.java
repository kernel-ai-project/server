package org.example.server.chatroom;

import reactor.core.publisher.Mono;

public interface AuthenticatedUserProvider {
    Mono<Long> getCurrentUserId();
}
