package org.example.server.chatRoom;

import reactor.core.publisher.Mono;

public interface AuthenticatedUserProvider {
    Mono<Long> getCurrentUserId();
}
