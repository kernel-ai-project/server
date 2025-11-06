package org.example.server.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.common.ApiResponse; // 새로운 ApiResponse
import org.example.server.social.dto.CustomOAuth2User; // 팀의 인증 객체
import org.example.server.user.dto.GreetingResponseDto;
import org.example.server.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    /**
     * 로그인한 유저 이름 표시 API
     * (새로운 ApiResponse 및 OAuth2 인증 방식 적용)
     */
    @GetMapping("/greetings")
    public ResponseEntity<ApiResponse<GreetingResponseDto>> getGreetings(
            @AuthenticationPrincipal CustomOAuth2User user) { // 팀의 인증 방식 적용

        // 1. 인증된 사용자 ID 획득
        Long loggedInUserId = user.getUserId();

        // 2. 서비스 호출 (수정된 DTO 반환)
        GreetingResponseDto greetingDto = userService.getGreeting(loggedInUserId);

        // 3. 새로운 ApiResponse 형식으로 성공 응답 생성
        ApiResponse<GreetingResponseDto> response = ApiResponse.success(
                "성공적으로 인사문구를 불러왔습니다.",
                greetingDto
        );

        return ResponseEntity.ok(response);
    }
}
