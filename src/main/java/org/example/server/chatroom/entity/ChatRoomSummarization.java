package org.example.server.chatroom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.user.entity.User;

@Entity
@Table(name = "chat_room_summarization")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class ChatRoomSummarization {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatRoom_summarization_seq_gen")
    @SequenceGenerator(
            name = "chatRoom_summarization_seq_gen",
            sequenceName = "chatRoom_summarization_seq"
    )
    private Long chatRoomSummarizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String summarizationContent;

}
