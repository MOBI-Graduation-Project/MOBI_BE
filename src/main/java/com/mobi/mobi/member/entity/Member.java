package com.mobi.mobi.member.entity;

import com.mobi.mobi.common.entity.BaseEntity;
import com.mobi.mobi.member.entity.enums.Avatar;
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

    @Column(unique=true, length = 20)
    private String nickname;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "is_privacy_agreed")
    private Boolean isPrivacyAgreed;

    @Column(name = "profile_img_url")
    private String profileImgUrl;

    @Column(name = "profile_describe")
    private String profileDescribe;

    @Enumerated(EnumType.STRING) // Enum의 이름을 DB에 문자열로 저장
    private Avatar avatar;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @Column(name = "investment_answers")
    private String investmentAnswers;

    @Column(name = "is_signed_up", nullable = false)
    private boolean isSignedUp = false;

    //token관련
    @Column(name = "refresh_token")
    private String refreshToken;

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    @Builder
    public Member(String username, String email, String profileImgUrl, LoginType loginType) {
        this.username = username;
        this.email = email;
        this.profileImgUrl = profileImgUrl;
        this.loginType = loginType;
        this.isSignedUp = false;
    }

    public Member update(String username) {
        // 구글에서 이름이 변경된 경우에만 업데이트
        this.username = username;
        // profileImgUrl 업데이트 로직 제거
        return this;
    }

    //최종 회원가입 완료
    public void completeSignup() {
        this.isSignedUp = true;
    }
}
