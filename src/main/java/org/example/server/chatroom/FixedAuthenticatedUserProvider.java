package org.example.server.chatroom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FixedAuthenticatedUserProvider implements AuthenticatedUserProvider {

    private final Long defaultUserId;

    public FixedAuthenticatedUserProvider(@Value("${chat.default-user-id:1}") Long defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    @Override
    public Mono<Long> getCurrentUserId() {
        return Mono.just(defaultUserId);
    }
}
