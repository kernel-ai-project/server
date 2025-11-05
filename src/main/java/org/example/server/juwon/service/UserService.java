package org.example.server.juwon.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.server.juwon.entity.User;
import org.example.server.juwon.dto.GreetingResponseDto;
import org.example.server.juwon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public GreetingResponseDto getGreeting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. ID: " + userId));

        // 요구사항: "로그인한 유저의 이름(username)을 기반으로"
        String greetingMessage = user.getUsername() + "님 안녕하세요";

        return new GreetingResponseDto(
                "성공적으로 인사문구를 불러왔습니다.",
                greetingMessage
        );
    }
}
