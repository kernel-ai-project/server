package org.example.server.chat.controller;

import org.example.server.chat.dto.AnswerRequest;
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



    //todo: 답변 저장 & 답변 반환
    @PostMapping(value = "/chatRooms/{chatRoomId}/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askStream(
            @PathVariable Long chatRoomId,
            @RequestBody AskRequest req) {

        Long userId = 6L;
        String question = req.question();

        // 질문 저장
        chatService.saveQuestion(userId, question, chatRoomId, true);

        // 답변 생성 및 반환 (답변 저장은 서비스에서 처리)
        return chatService.askStreamWithContext(userId, chatRoomId, question);
    }

    /**
     * test : Redis에 저장된 요약 조회
     */
    @GetMapping("/chatRooms/{chatRoomId}/summary")
    public ResponseEntity<String> getChatSummary(@PathVariable Long chatRoomId) {
        
        String summary = chatRedisService.getSummary(chatRoomId);
        System.out.println("summary = " + summary);
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