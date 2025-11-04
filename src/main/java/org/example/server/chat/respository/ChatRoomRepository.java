package org.example.server.chat.respository;

import jakarta.persistence.LockModeType;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr.chatRoomId, cr.title, cr.isFavorited " +
            "FROM ChatRoom cr " +
            "WHERE cr.user.userId = :userId")
    List<ChatRoomResponse> findByUserId(@Param("userId") Long userId);

    boolean existsByUser_UserIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT cr " +
            "FROM ChatRoom cr " +
            "WHERE cr.user.userId = :userId AND cr.chatRoomId = :chatRoomId")
    Optional<ChatRoom> findByUserIdAndChatRoomId(@Param("userId") Long userId,
                                                @Param("chatRoomId") Long chatRoomId);

}