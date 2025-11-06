package org.example.server.chat.repository;

import org.example.server.chat.dto.SummarizeResponse;
import org.example.server.chatroom.entity.ChatRoomSummarization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomSummarizationRepository extends JpaRepository<ChatRoomSummarization, Long> {
    @Query("SELECT new org.example.server.chat.dto.SummarizeResponse(" +
            "s.summarizationContent, " +
            "SIZE(s.chatRoom.messages)) " +
            "FROM ChatRoomSummarization s " +
            "WHERE s.chatRoom.chatRoomId = :chatRoomId")
    SummarizeResponse findByChatRoomChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
