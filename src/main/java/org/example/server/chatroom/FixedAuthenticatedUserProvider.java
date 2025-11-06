package org.example.server.chatroom;

import org.example.server.social.dto.CustomOAuth2User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FixedAuthenticatedUserProvider implements AuthenticatedUserProvider {

    @Override
    public Mono<Long> getCurrentUserId() {
        return Mono.defer(() -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof CustomOAuth2User customUser) {
                    return Mono.just(customUser.getUserId());
                }
            }

            return Mono.error(new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));
        });
    }
}
