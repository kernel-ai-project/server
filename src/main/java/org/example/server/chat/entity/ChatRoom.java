package org.example.server.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ChatRoom")
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

    @Column(name = "is_deleted", columnDefinition = "NUMBER(1)")
    private Boolean isDeleted;

    @Column(name = "is_favorited", insertable = false, columnDefinition = "NUMBER(1)")
    private Boolean isFavorited;

    public void addFavorite() {
        if (this.isFavorited) {
            throw new IllegalStateException("이미 즐겨찾기된 채팅방입니다.");
        }

        this.isFavorited = true;
    }
}