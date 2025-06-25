package com.group3.askmyfriend.dto;

import com.group3.askmyfriend.entity.ChatRoomEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ChatRoomDTO {
    private Long roomId;
    private String roomName;
    private String roomType;
    private String createdAt;
    private String lastMessageAt;
    private Boolean isActive;
    private List<ChatParticipantDTO> participants;
    private ChatMessageDTO lastMessage;
    private Long unreadCount;

    public ChatRoomDTO(ChatRoomEntity entity) {
        this.roomId = entity.getRoomId();
        this.roomName = entity.getRoomName();
        this.roomType = entity.getRoomType().name();
        this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.lastMessageAt = entity.getLastMessageAt() != null ? 
            entity.getLastMessageAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        this.isActive = entity.getIsActive();
        this.participants = entity.getParticipants().stream()
            .filter(p -> p.getIsActive())
            .map(ChatParticipantDTO::new)
            .collect(Collectors.toList());
    }

    // 1:1 채팅방의 상대방 이름을 가져오는 메서드
    public String getDisplayName(Long currentUserId) {
        if ("PRIVATE".equals(roomType) && participants.size() == 2) {
            return participants.stream()
                .filter(p -> !p.getUserId().equals(currentUserId))
                .findFirst()
                .map(ChatParticipantDTO::getUserNickname)
                .orElse("알 수 없는 사용자");
        }
        return roomName != null ? roomName : "채팅방";
    }
}