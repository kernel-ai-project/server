package org.example.server.chatroom.service;

import org.example.server.chat.dto.SummarizeResponse;

public interface ChatRoomSummarizationService {
    SummarizeResponse findSummarizationByChatRoomId(Long chatRoomId);
}