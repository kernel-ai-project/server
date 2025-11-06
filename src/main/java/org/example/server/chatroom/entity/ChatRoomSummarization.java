package org.example.server.chatroom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.common.BaseTimeEntity;
import org.example.server.user.entity.User;

@Entity
@Table(name = "chat_room_summarization")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class ChatRoomSummarization extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatRoom_summarization_seq_gen")
    @SequenceGenerator(
            name = "chatRoom_summarization_seq_gen",
            sequenceName = "chatRoom_summarization_seq"
    )
    private Long chatRoomSummarizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private String summarizationContent;

}
