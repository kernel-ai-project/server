package org.example.server.chatroom.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.server.chat.dto.ChatMessage;
import org.example.server.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByChatRoom_ChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
    // Repository
    @Query("SELECT new org.example.server.chat.dto.ChatMessage$HistoryMessageDTO(m.content, m.isUser) " +
            "FROM Message m " +
            "WHERE m.chatRoom.chatRoomId = :chatRoomId " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessage.HistoryMessageDTO> findTop10ByChatRoomId(@Param("chatRoomId") Long chatRoomId,
                                                              Pageable pageable);


}
