package com.group3.askmyfriend.dto;

import com.group3.askmyfriend.entity.UserEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSummaryDTO {
    private Long userId;
    private String loginId;
    private String nickname;
    private String profileImg;
    private String statusMessage;
    private boolean isFollowing;  // 새로 추가한 필드 (팔로잉 여부)

    public UserSummaryDTO(UserEntity user) {
        this.userId = user.getUserId();
        this.loginId = user.getLoginId();
        this.nickname = user.getNickname();
        this.profileImg = user.getProfileImg();
        this.statusMessage = user.getBio(); // 상태 메시지를 bio로 사용 중이면 여기 반영
        this.isFollowing = false;  // 기본 false, 나중에 서비스에서 세팅

    }
}
