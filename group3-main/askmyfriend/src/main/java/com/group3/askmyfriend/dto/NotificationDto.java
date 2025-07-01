package com.group3.askmyfriend.dto;

import com.group3.askmyfriend.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String message;
    private String url;
    private LocalDateTime createdAt;
    private boolean isRead;
    
    // 발신자 정보 (프로필 사진 표시용)
    private Long senderId;
    private String senderNickname;
    private String senderProfileImg;
    
    // 수신자 정보
    private Long receiverId;
    private String receiverNickname;
    
    // 대상 리소스 ID
    private Long targetId;
    
    // 편의 메서드들
    public boolean getRead() {
        return this.isRead;
    }
    
    public void setRead(boolean read) {
        this.isRead = read;
    }
    
    // 발신자가 있는지 확인
    public boolean hasSender() {
        return senderId != null;
    }
    
    // 프로필 이미지가 있는지 확인
    public boolean hasProfileImage() {
        return senderProfileImg != null && !senderProfileImg.isEmpty();
    }
    
    // 발신자 닉네임 첫 글자 (기본 아바타용)
    public String getSenderInitial() {
        if (senderNickname != null && !senderNickname.isEmpty()) {
            return senderNickname.substring(0, 1).toUpperCase();
        }
        return "?";
    }
}
