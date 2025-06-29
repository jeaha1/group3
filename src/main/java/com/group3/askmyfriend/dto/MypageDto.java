package com.group3.askmyfriend.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MypageDto {
    private String loginId;          // ⭐️ 이 필드가 필요함
    private String username;         // 닉네임
    private String userId;           // 로그인 아이디 (loginId와 같은 값)
    private String bio;
    private String profileImg;
    private String backgroundImg;
    private int followingCount;
    private int followerCount;
    private LocalDateTime createdAt; // 가입일
    private String privacy;          // 공개범위

    // ⭐️ 기본 생성자 추가 (필수)
    public MypageDto() {}

    // ⭐️ 생성자에 loginId 추가
    public MypageDto(String loginId, String username, String userId, String bio, String profileImg, String backgroundImg,
                     int followingCount, int followerCount, LocalDateTime createdAt, String privacy) {
        this.loginId = loginId;      // ⭐️ 추가
        this.username = username;
        this.userId = userId;
        this.bio = bio;
        this.profileImg = profileImg;
        this.backgroundImg = backgroundImg;
        this.followingCount = followingCount;
        this.followerCount = followerCount;
        this.createdAt = createdAt;
        this.privacy = privacy;
    }
}
