package org.example.server.chat.controller;

import org.example.server.chat.dto.ChatResponse;
import org.example.server.chat.service.ChatRedisService;
import org.example.server.chat.service.ChatService;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // 외부에 노출되는 단일 엔드포인트
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatRedisService chatRedisService;

    @PostMapping(value = "/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AskResponse> ask(@RequestBody AskRequest req) {
        return chatService.ask(req);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askStream(@RequestBody AskRequest req) {
        return chatService.askStream(req);
    }


    //todo: 답변 저장 & 답변 반환
    @PostMapping(value = "/chatRooms/{chatRoomId}/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askStream(
            @PathVariable Long chatRoomId,
            @RequestBody AskRequest req) {

        Long userId = 6L;

        //질문
        String question = req.question();

        // 답변
        String answer = chatService.askStream(req)
                .collectList()  // Flux<String> → Mono<List<String>>
                .map(chunks -> String.join("", chunks))  // List<String> → String (하나로 합치기)
                .block();

        //반환
        String saveQuestion = chatService.saveMessage(userId, question, chatRoomId, true);
        String saveAnswer = chatService.saveMessage(userId, answer, chatRoomId, false);

        Flux<String> fluxAnswer = Flux.just(saveAnswer);

        return fluxAnswer;
    }

    /**
     * test : Redis에 저장된 요약 조회
     */
    @GetMapping("/chatRooms/{chatRoomId}/summary")
    public ResponseEntity<String> getChatSummary(@PathVariable Long chatRoomId) {
        String summary = chatRedisService.getSummary(chatRoomId);
        if (summary == null) {
            return ResponseEntity.ok("아직 요약이 생성되지 않았습니다.");
        }
        return ResponseEntity.ok(summary);
    }


    /**
     * test :  Redis에 저장된 최근 대화 조회
     */
    @GetMapping("/chatRooms/{chatRoomId}/recent")
    public ResponseEntity<List<Object>> getRecentChatHistory(@PathVariable Long chatRoomId) {
        List<Object> history = chatRedisService.getRecentHistory(chatRoomId);
        return ResponseEntity.ok(history);
    }
}