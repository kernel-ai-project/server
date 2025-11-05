package org.example.server.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.chat.dto.ChatMessage;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService{

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key prefix
    private static final String CHAT_HISTORY_KEY = "chat:history:";
    private static final String CHAT_SUMMARY_KEY = "chat:summary:";

    // Redis 저장 설정
    private static final long TTL_MINUTES = 30;
    private static final int MAX_HISTORY_COUNT = 10;

    // 요약 조건 설정
    private static final int SUMMARY_TRIGGER_COUNT = 10; // 메시지 10개마다 요약
    private Instant lastSummaryTime = Instant.now();
    private int messageSinceLastSummary = 0;

    //todo: Redis에 메시지 저장 (유저 or 챗봇)
    @Override
    public void saveMessage(Long chatRoomId, boolean isUser, String message) {
        String key = CHAT_HISTORY_KEY + chatRoomId;
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        // user / bot 구분하여 저장
        String formattedMessage = (isUser ? "user:" : "bot:") + message;

        // 새로운 메시지 추가
        listOps.rightPush(key, formattedMessage);

        // 최신 10개(=5세트)만 유지
        Long size = listOps.size(key);
        if (size != null && size > 8) {
            long toRemove = size - 8;
            for (int i = 0; i < toRemove; i++) {
                listOps.leftPop(key);
            }
        }

        // TTL 재설정
        redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);

        // 요약 트리거 카운터 증가
        messageSinceLastSummary++;

        //요약 트리거 조건 확인
        if (shouldSummarize()) {
            log.info("요약 트리거 실행! chatRoomId={}", chatRoomId);
            summarizeAndStore(chatRoomId);
            lastSummaryTime = Instant.now();
            messageSinceLastSummary = 0;
        }
    }

    //todo: 저장된 요약문 조회
    @Override
    public String getSummary(Long chatRoomId) {
        String key = CHAT_SUMMARY_KEY + chatRoomId;
        Object summary = redisTemplate.opsForValue().get(key);
        return summary != null ? summary.toString() : null;
    }


    //todo: 최근 대화 5세트 (10메시지) 조회
    @Override
    public List<Object> getRecentHistory(Long chatRoomId) {
        String key = CHAT_HISTORY_KEY + chatRoomId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }


    //todo: 요약 트리거 조건: 메시지 10개 누적 or 15분 경과
    private boolean shouldSummarize() {
        boolean enoughMessages = messageSinceLastSummary >= SUMMARY_TRIGGER_COUNT;
        boolean timeElapsed = Duration.between(lastSummaryTime, Instant.now()).toMinutes() >= 15;
        return enoughMessages || timeElapsed;
    }

    //todo: 요약 생성 및 저장 (임시 Placeholder — FastAPI 연동 예정)
    private void summarizeAndStore(Long chatRoomId) {
        String summaryKey = CHAT_SUMMARY_KEY + chatRoomId;

        // 최근 대화 가져오기
        List<Object> history = getRecentHistory(chatRoomId);

        //(임시 요약) — 실제론 FastAPI에 요청할 예정
        String fakeSummary = "[요약] 최근 대화 " + history.size() + "개 요약 완료";

        // 3️⃣ Redis에 저장
        redisTemplate.opsForValue().set(summaryKey, fakeSummary, Duration.ofMinutes(TTL_MINUTES));

        log.info("요약 저장 완료: {}", fakeSummary);
    }

}