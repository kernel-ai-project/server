package org.example.server.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Chat_Room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @Column(name = "chat_room_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatRoom_seq_gen")
    @SequenceGenerator(
            name = "chatRoom_seq_gen",
            sequenceName = "chatRoom_seq"
            // allocationSize = 50 (기본값)
    )
    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted", columnDefinition = "NUMBER(1)")
    private Boolean isDeleted;

    @Column(name = "is_favorited", insertable = false, columnDefinition = "NUMBER(1)")
    private Boolean isFavorited;

}