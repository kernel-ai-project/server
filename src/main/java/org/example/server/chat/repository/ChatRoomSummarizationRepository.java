package org.example.server.chat.repository;

import org.example.server.chat.dto.SummarizeResponse;
import org.example.server.chatroom.entity.ChatRoomSummarization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomSummarizationRepository extends JpaRepository<ChatRoomSummarization, Long> {
    SummarizeResponse findByChatRoomId(Long chatRoomId);
}
