package com.group3.askmyfriend.dto;

import com.group3.askmyfriend.entity.ChatMessageEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class ChatMessageDTO {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private String messageType;
    private String sentAt;
    private Boolean isDeleted;

    public ChatMessageDTO(ChatMessageEntity entity) {
        this.messageId = entity.getMessageId();
        this.roomId = entity.getChatRoom().getRoomId();
        this.senderId = entity.getSender().getUserId();
        this.senderNickname = entity.getSender().getNickname();
        this.content = entity.getContent();
        this.messageType = entity.getMessageType().name();
        this.sentAt = entity.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isDeleted = entity.getIsDeleted();
    }

    // WebSocket으로 전송할 때 사용하는 생성자
    public ChatMessageDTO(Long roomId, Long senderId, String senderNickname, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.messageType = "TEXT";
        this.sentAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isDeleted = false;
    }
}