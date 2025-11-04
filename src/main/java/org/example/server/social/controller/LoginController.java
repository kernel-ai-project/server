package org.example.server.social.controller;

import org.example.server.social.dto.CustomOAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @GetMapping("/login-test")
    public String loginPage(@AuthenticationPrincipal CustomOAuth2User user) {

        Long userId = user.getUserId();

        System.out.println("userId = " + userId);
        return "login 완료";
    }
}