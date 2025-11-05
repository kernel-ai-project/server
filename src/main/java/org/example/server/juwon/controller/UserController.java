package org.example.server.juwon.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.juwon.common.dto.ApiResponse;
import org.example.server.juwon.dto.GreetingResponseDto;
import org.example.server.juwon.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController
{
    private final UserService userService;

    @GetMapping("/greetings")
    public ApiResponse<GreetingResponseDto> getGreetings() {
        // [임시] Spring Security 등 인증 컨텍스트가 없으므로,
        // "로그인한 유저"의 ID를 1L로 가정합니다.
        Long loggedInUserId = 1L;

        GreetingResponseDto result = userService.getGreeting(loggedInUserId);
        return ApiResponse.success(result);
    }
}
