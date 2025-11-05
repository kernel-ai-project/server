package org.example.server.chat.controller;

import org.example.server.chat.dto.ChatResponse;
import org.example.server.chat.service.ChatService;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api") // 외부에 노출되는 단일 엔드포인트
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AskResponse> ask(@RequestBody AskRequest req) {
        return chatService.ask(req);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askStream(@RequestBody AskRequest req) {
        return chatService.askStream(req);
    }

    /**
     * 질문 시 대화내역(질문, 답변) 저장
     * */
    @PostMapping("/chatRooms/{chatRoomId}/chat")
    public ResponseEntity<ChatResponse.SaveChatQuestionAndAnswerDTO> saveChatQuestionAndAnswerHistory(
            @PathVariable Long chatRoomId,
            @RequestBody AskRequest req){
        Long userId = 6L;
        String question = req.question();

        String answer = chatService.ask(req)
                .map(AskResponse::answer)
                .block();

        Map<String, Long> questioneMap = chatService.saveMessage(userId, question, chatRoomId, true);
        Map<String, Long> answerMap = chatService.saveMessage(userId, answer, chatRoomId, false);

        Long saveQuestionId = questioneMap.get("messageId");
        Long saveChatRoomId = questioneMap.get("chatRoomId");
        Long saveAnswerId = answerMap.get("messageId");

        ChatResponse.SaveChatQuestionAndAnswerDTO chatQuestionAndAnswerDTO = ChatResponse.SaveChatQuestionAndAnswerDTO.builder()
                .chatAnswerId(saveAnswerId)
                .chatQuestionId(saveQuestionId)
                .chatRoomId(saveChatRoomId)
                .build();

        return ResponseEntity.ok(chatQuestionAndAnswerDTO);
    }
}