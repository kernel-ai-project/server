package org.example.server.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.server.chatroom.entity.ChatRoom;
import org.example.server.common.BaseTimeEntity;

@Entity
@Table(name = "Message")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseTimeEntity {

    @Id
    @Column(name = "message_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mes_seq_gen")
    @SequenceGenerator(
            name = "mes_seq_gen",
            sequenceName = "mes_seq"
            // allocationSize = 50 (기본값)
    )
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoomId")
    private ChatRoom chatRoom;

    @Column(name = "is_user", nullable = false, columnDefinition = "NUMBER(1)")
    private boolean isUser;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String content;

}