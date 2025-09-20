package com.mobi.mobi.friend.entity;

import com.mobi.mobi.common.entity.BaseEntity;
import com.mobi.mobi.friend.entity.enums.FriendStatus;
import com.mobi.mobi.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Friend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    // 친구 관계를 요청한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    // 친구 관계를 요청받은 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    // 친구 관계의 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    @Builder
    public Friend(Member fromMember, Member toMember, FriendStatus status) {
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.status = status;
    }

    // 친구 요청 수락 시 상태를 변경하는 메서드
    public void acceptRequest() {
        this.status = FriendStatus.ACCEPTED;
    }

    // 친구 요청 거절 시 상태를 변경하는 메서드
    public void declineRequest() {
        this.status = FriendStatus.DECLINED;
    }
}
