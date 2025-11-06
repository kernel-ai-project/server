package org.example.server.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.chat.dto.ChatMessage;
import org.example.server.chat.dto.SummarizeRequest;
import org.example.server.chat.dto.SummarizeResponse;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient fastapiClient;

    // Redis key prefix
    private static final String CHAT_HISTORY_KEY = "chat:history:";
    private static final String CHAT_SUMMARY_KEY = "chat:summary:";
    private static final String CHAT_COUNTER_KEY = "chat:counter:";  // 추가
    private static final String CHAT_LAST_SUMMARY_TIME_KEY = "chat:lastSummaryTime:";  // 추가

    // Redis 저장 설정
    private static final long TTL_MINUTES = 30;
    private static final int MAX_HISTORY_COUNT = 10;

    // 요약 조건 설정
    private static final int SUMMARY_TRIGGER_COUNT = 10;

    @Override
    public void saveMessage(Long chatRoomId, boolean isUser, String message) {
        String key = CHAT_HISTORY_KEY + chatRoomId;
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        // user / bot 구분하여 저장
        String formattedMessage = (isUser ? "user:" : "assistant:") + message;

        // 새로운 메시지 추가
        listOps.rightPush(key, formattedMessage);

        // 최신 10개만 유지
        Long size = listOps.size(key);
        if (size != null && size > MAX_HISTORY_COUNT) {
            long toRemove = size - MAX_HISTORY_COUNT;
            for (int i = 0; i < toRemove; i++) {
                listOps.leftPop(key);
            }
        }

        // TTL 재설정
        redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);

        // 채팅방별 카운터 증가
        incrementMessageCounter(chatRoomId);

        // 요약 트리거 조건 확인
        if (shouldSummarize(chatRoomId)) {
            log.info("요약 트리거 실행! chatRoomId={}", chatRoomId);
            summarizeAndStore(chatRoomId);
            resetMessageCounter(chatRoomId);
            updateLastSummaryTime(chatRoomId);
        }
    }

    @Override
    public String getSummary(Long chatRoomId) {
        String key = CHAT_SUMMARY_KEY + chatRoomId;
        Object summary = redisTemplate.opsForValue().get(key);
        return summary != null ? summary.toString() : null;
    }

    @Override
    public List<Object> getRecentHistory(Long chatRoomId) {
        String key = CHAT_HISTORY_KEY + chatRoomId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }


    //todo: 채팅방 열었을 때 최근 대화내역 조히
    @Override
    public void setChatHistoryOriginChatRoom(Long chatRoomId , List<ChatMessage.HistoryMessageDTO> historyMessageDTOList){
        String key = CHAT_HISTORY_KEY + chatRoomId;

        for(ChatMessage.HistoryMessageDTO message : historyMessageDTOList){
            ListOperations<String,Object> listOps = redisTemplate.opsForList();

            // user / bot 구분하여 저장
            String formattedMessage = (message.getIsUser() ? "user:" : "assistant:") + message;

            // 새로운 메시지 추가
            listOps.rightPush(key, formattedMessage);

        }
    }

    //todo: 채팅방 열었을 때 요약본 조회 후 세팅
    @Override
    public void setChatSummaryOriginChatRoom(Long chatRoomId, SummarizeResponse summary){
        try {
            String summaryKey = CHAT_SUMMARY_KEY + chatRoomId;

            if (summary == null) {
                log.warn("요약했던 내용이 없습니다.");
            }else{
                redisTemplate.opsForValue().set(
                        summaryKey,
                        summary.getSummary(),
                        Duration.ofMinutes(TTL_MINUTES)
                );
            }
        }catch (Exception e){
            log.error("요약 불러오기 실패");
        }
    }

    /**
     * 채팅방별 메시지 카운터 증가
     */
    private void incrementMessageCounter(Long chatRoomId) {
        String counterKey = CHAT_COUNTER_KEY + chatRoomId;
        redisTemplate.opsForValue().increment(counterKey);
        redisTemplate.expire(counterKey, TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 채팅방별 메시지 카운터 리셋
     */
    private void resetMessageCounter(Long chatRoomId) {
        String counterKey = CHAT_COUNTER_KEY + chatRoomId;
        redisTemplate.opsForValue().set(counterKey, "0");
        redisTemplate.expire(counterKey, TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 채팅방별 메시지 카운터 조회
     */
    private int getMessageCounter(Long chatRoomId) {
        String counterKey = CHAT_COUNTER_KEY + chatRoomId;
        Object counter = redisTemplate.opsForValue().get(counterKey);
        return counter != null ? Integer.parseInt(counter.toString()) : 0;
    }

    /**
     * 채팅방별 마지막 요약 시간 업데이트
     */
    private void updateLastSummaryTime(Long chatRoomId) {
        String timeKey = CHAT_LAST_SUMMARY_TIME_KEY + chatRoomId;
        redisTemplate.opsForValue().set(timeKey, Instant.now().toString());
        redisTemplate.expire(timeKey, TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 채팅방별 마지막 요약 시간 조회
     */
    private Instant getLastSummaryTime(Long chatRoomId) {
        String timeKey = CHAT_LAST_SUMMARY_TIME_KEY + chatRoomId;
        Object time = redisTemplate.opsForValue().get(timeKey);
        return time != null ? Instant.parse(time.toString()) : Instant.now();
    }

    /**
     * 요약 트리거 조건: 메시지 10개 누적 or 15분 경과
     */
    private boolean shouldSummarize(Long chatRoomId) {
        int counter = getMessageCounter(chatRoomId);
        boolean enoughMessages = counter >= SUMMARY_TRIGGER_COUNT;

        Instant lastTime = getLastSummaryTime(chatRoomId);
        boolean timeElapsed = Duration.between(lastTime, Instant.now()).toMinutes() >= 15;

        return enoughMessages || timeElapsed;
    }


    /**
     * FastAPI를 호출하여 요약 생성 및 Redis 저장
     */
    private void summarizeAndStore(Long chatRoomId) {
        try {
            String summaryKey = CHAT_SUMMARY_KEY + chatRoomId;

            // 1. 기존 요약 가져오기 (추가!)
            String previousSummary = getSummary(chatRoomId);

            // 2. 최근 대화 가져오기
            List<Object> history = getRecentHistory(chatRoomId);
            System.out.println("히스토리 출력 : " + history.toString());

            if (history == null || history.isEmpty()) {
                log.warn("요약할 대화 내역이 없습니다. chatRoomId={}", chatRoomId);
                return;
            }

            List<ChatMessage> messages = convertToFastApiFormat(history);

            // 3. FastAPI 요약 API 호출
            SummarizeRequest request = new SummarizeRequest(messages,previousSummary);

            SummarizeResponse response = fastapiClient.post()
                    .uri("/summarize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SummarizeResponse.class)
                    .block();

            System.out.println("요약 내용 : " + response.getSummary());

            if (response != null && response.getSummary() != null) {
                // 4. Redis에 요약 저장
                redisTemplate.opsForValue().set(
                        summaryKey,
                        response.getSummary(),
                        Duration.ofMinutes(TTL_MINUTES)
                );

                log.info("요약 저장 완료: chatRoomId={}, summary={}", chatRoomId, response.getSummary());
            }

        } catch (Exception e) {
            log.error("요약 생성 실패: chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
        }
    }

    /**
     * Redis 형식을 FastAPI 형식으로 변환
     */
    private List<ChatMessage> convertToFastApiFormat(List<Object> redisHistory) {
        List<ChatMessage> messages = new ArrayList<>();

        for (Object obj : redisHistory) {
            String message = obj.toString();

            if (message.startsWith("user:")) {
                messages.add(new ChatMessage("user", message.substring(5)));
            } else if (message.startsWith("assistant:") || message.startsWith("bot:")) {
                String content = message.startsWith("assistant:")
                        ? message.substring(10)
                        : message.substring(4);
                messages.add(new ChatMessage("assistant", content));
            }
        }

        return messages;
    }
}