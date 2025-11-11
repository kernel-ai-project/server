package org.example.server.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.exception.UserNotFoundException;
import org.example.server.user.entity.User;
import org.example.server.user.dto.GreetingResponseDto;
import org.example.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public GreetingResponseDto getGreeting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String greetingMessage = user.getNickname() + "님 안녕하세요";

        // "message"가 없는 새로운 DTO를 반환
        return new GreetingResponseDto(greetingMessage);
    }
}
