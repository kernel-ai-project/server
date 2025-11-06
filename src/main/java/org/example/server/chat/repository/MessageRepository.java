package org.example.server.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.server.chat.dto.ChatMessage;
import org.example.server.chat.entity.Message;
import org.example.server.chatroom.dto.ChatMessageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Repository
    @Query("SELECT new org.example.server.chat.dto.ChatMessage.HistoryMessageDTO(m.content, m.isUser) " +
            "FROM Message m " +
            "WHERE m.chatRoom.chatRoomId = :chatRoomId " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessage.HistoryMessageDTO> findTop10ByChatRoomId(@Param("chatRoomId") Long chatRoomId,
                                                              Pageable pageable);}
