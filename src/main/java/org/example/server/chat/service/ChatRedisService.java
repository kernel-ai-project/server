package org.example.server.chat.service;

import org.springframework.data.redis.core.ListOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface ChatRedisService {
    void saveMessage(Long chatRoomId, boolean isUser, String message);
    String getSummary(Long chatRoomId);
    List<Object> getRecentHistory(Long chatRoomId);

}
