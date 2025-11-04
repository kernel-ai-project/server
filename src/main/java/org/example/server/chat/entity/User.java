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
@Table(name = "Users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(
            name = "user_seq_gen", // JPA 내부에서 참조할 이름
            sequenceName = "user_seq" // 실제 DB 시퀀스 이름
            // allocationSize = 50 (기본값)
    )
    private Long userId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<ChatRoom> chatRooms = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "social_type")
    private String socialType;

    private String gender;

    @Column(name = "is_deleted", columnDefinition = "NUMBER(1)")
    private boolean isDeleted;

}