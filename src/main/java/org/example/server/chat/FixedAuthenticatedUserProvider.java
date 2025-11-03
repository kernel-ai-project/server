package org.example.server.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FixedAuthenticatedUserProvider implements AuthenticatedUserProvider {

    private final Long defaultUserId;

    public FixedAuthenticatedUserProvider(@Value("${chat.default-user-id:1}") Long defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    @Override
    public Long getCurrentUserId() {
        return defaultUserId;
    }
}
