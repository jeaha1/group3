package com.group3.askmyfriend.entity;

public enum NotificationType {
    LIKE("좋아요"),           // 추가!
    FOLLOW("팔로우"),
    COMMENT("댓글"),
    MENTION("멘션"),
    SYSTEM("시스템"),
    FRIEND_REQUEST("친구 요청"),
    GROUP_INVITE("그룹 초대");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
