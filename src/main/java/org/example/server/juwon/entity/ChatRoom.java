package org.example.server.juwon.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CHAT_ROOM")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom
{

    @Id
    @Column(name = "chat_room_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatRoom_seq_gen")
    @SequenceGenerator(name = "chatroom_seq", sequenceName = "chatroom_seq", allocationSize = 1)
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

    /**
     * 채팅방 제목을 수정합니다. (JPA 변경 감지를 위해)
     * @param newTitle 새로운 제목
     */
    public void updateTitle(String newTitle) {
        this.title = newTitle;
        // updateAt 필드가 있다면 여기서 함께 갱신할 수 있습니다.
        // this.updateAt = LocalDateTime.now();
    }
}