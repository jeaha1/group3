package com.group3.askmyfriend.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MypageDto {
    private String username;        // 닉네임
    private String userId;          // 로그인 아이디
    private String bio;
    private String profileImg;
    private String backgroundImg;
    private int followingCount;
    private int followerCount;
    private LocalDateTime createdAt; // 가입일
    private String privacy;          // 공개범위 추가

    public MypageDto(String username, String userId, String bio, String profileImg, String backgroundImg,
                     int followingCount, int followerCount, LocalDateTime createdAt, String privacy) {
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
