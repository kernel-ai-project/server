package org.example.server.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.chatroom.dto.GreetingResponse;
import org.example.server.chatroom.dto.GreetingResult;
import org.example.server.chatroom.service.ChatRoomService;
import org.example.server.social.dto.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class GreetingController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/greetings")
    public ResponseEntity<GreetingResponse> getGreetings(@AuthenticationPrincipal CustomOAuth2User user) {

        Long userId = user.getUserId();
        String greeting = chatRoomService.findGreeting(userId);

        return ResponseEntity
                .ok()
                .body(
                        new GreetingResponse(true,
                                new GreetingResult("성공적으로 인사문구를 불러왔습니다.", greeting)
                        )
                );
    }
}