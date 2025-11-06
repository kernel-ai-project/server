package org.example.server.chatroom.service;

import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.SummarizeResponse;
import org.example.server.chat.repository.ChatRoomSummarizationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomSummarizationServiceImpl implements ChatRoomSummarizationService {

    private final ChatRoomSummarizationRepository chatRoomSummarizationRepository;

    @Override
    public SummarizeResponse findSummarizationByChatRoomId(Long chatRoomId){
        return chatRoomSummarizationRepository.findByChatRoomId(chatRoomId);
    }
}
