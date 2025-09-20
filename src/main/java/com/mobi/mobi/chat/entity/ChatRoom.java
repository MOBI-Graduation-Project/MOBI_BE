package com.mobi.mobi.chat.entity;

import com.mobi.mobi.chat.entity.enums.ChatType;
import com.mobi.mobi.common.entity.BaseEntity;
import com.mobi.mobi.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName; // 채팅방 이름 (e.g., "A와 B의 채팅", "삼성전자 토론방")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType chatType; // 채팅방 종류

    // ChatRoomMember와의 양방향 관계 설정
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomMember> chatRoomMembers = new ArrayList<>();

    @Builder
    public ChatRoom(String roomName, ChatType chatType) {
        this.roomName = roomName;
        this.chatType = chatType;
    }
}