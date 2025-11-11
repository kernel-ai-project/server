package org.example.server.chatroom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.server.common.BaseTimeEntity;
import org.example.server.chat.entity.Message;
import org.example.server.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Chat_Room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

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
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(name = "is_deleted", insertable = false, columnDefinition = "NUMBER(1)")
    private Boolean isDeleted;

    @Column(name = "is_favorited", insertable = false, columnDefinition = "NUMBER(1)")
    private Boolean isFavorited;

    public void addFavorite() {
        if (this.isFavorited) {
            throw new IllegalStateException("이미 즐겨찾기된 채팅방입니다.");
        }

        this.isFavorited = true;
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
        // 필요시 updateAt 필드도 갱신
        // this.updateAt = LocalDateTime.now();
    }

    public void removeFavorite() {
        if (!this.isFavorited) {
            throw new IllegalStateException("즐겨찾기되지 않은 채팅방입니다.");
        }

        this.isFavorited = false;
    }
}