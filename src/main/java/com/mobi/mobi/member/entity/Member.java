package com.mobi.mobi.member.entity;

import com.mobi.mobi.common.entity.BaseEntity;
import com.mobi.mobi.member.entity.enums.LoginType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "is_privacy_agreed", nullable = false)
    private Boolean isPrivacyAgreed;

    @Column(name = "profile_img_url")
    private String profileImgUrl;

    private String avatar;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @Builder
    public Member(String username, String email, String profileImgUrl, LoginType loginType) {
        this.username = username;
        this.email = email;
        this.profileImgUrl = profileImgUrl;
        this.loginType = loginType;
    }

    // 구글 프로필 정보(이름, 사진)가 변경될 경우 업데이트하는 메서드
    public Member update(String username, String profileImgUrl) {
        this.username = username;
        this.profileImgUrl = profileImgUrl;
        return this;
    }
}
