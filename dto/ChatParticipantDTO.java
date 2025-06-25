package com.group3.askmyfriend.dto;

import com.group3.askmyfriend.entity.ChatParticipantEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class ChatParticipantDTO {
    private Long participantId;
    private Long roomId;
    private Long userId;
    private String userNickname;
    private String joinedAt;
    private String lastReadAt;
    private Boolean isActive;

    public ChatParticipantDTO(ChatParticipantEntity entity) {
        this.participantId = entity.getParticipantId();
        this.roomId = entity.getChatRoom().getRoomId();
        this.userId = entity.getUser().getUserId();
        this.userNickname = entity.getUser().getNickname();
        this.joinedAt = entity.getJoinedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.lastReadAt = entity.getLastReadAt() != null ? 
            entity.getLastReadAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        this.isActive = entity.getIsActive();
    }
}