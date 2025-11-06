package org.example.server.chat.service;

import org.example.server.chat.dto.ChatMessage;
import org.example.server.chat.dto.SummarizeResponse;
import org.springframework.data.redis.core.ListOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface ChatRedisService {
    void saveMessage(Long chatRoomId, boolean isUser, String message);
    String getSummary(Long chatRoomId);
    List<Object> getRecentHistory(Long chatRoomId);

    //todo: 채팅방 열었을 때 최근 대화내역 조히
    void setChatHistoryOriginChatRoom(Long chatRoomId , List<ChatMessage.HistoryMessageDTO> historyMessageDTOList);

    //todo: 채팅방 열었을 때 요약본 조회 후 세팅
    void setChatSummaryOriginChatRoom(Long chatRoomId, SummarizeResponse summary);
}
