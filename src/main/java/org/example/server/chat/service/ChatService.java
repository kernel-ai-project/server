package org.example.server.chat.service;

import jakarta.transaction.Transactional;
import org.example.server.chat.dto.AnswerRequest;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.chat.dto.ChatMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ChatService {
    Mono<AskResponse> ask(AskRequest req);

    Flux<String> askStream(AnswerRequest req);

    void saveQuestion(Long userId, String question, Long chatRoomId, Boolean is_user);

    Flux<String> askStreamWithContext(Long userId, Long chatRoomId, String question);

    List<ChatMessage.HistoryMessageDTO> findTop10ByChatRoomId(Long chatRoomId);

}
