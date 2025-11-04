package org.example.server.chatroom.repository;

import java.util.List;
import org.example.server.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByChatRoom_ChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
