package com.group3.askmyfriend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 100)
    private String roomName; // 그룹채팅 시에만 사용

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType; // PRIVATE(1:1), GROUP(그룹)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // 채팅방 참여자들과의 관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatParticipantEntity> participants = new ArrayList<>();

    // 채팅 메시지들과의 관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessageEntity> messages = new ArrayList<>();

    public enum RoomType {
        PRIVATE, GROUP
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    // 1:1 채팅방 생성용 편의 메서드
    public static ChatRoomEntity createPrivateRoom() {
        ChatRoomEntity room = new ChatRoomEntity();
        room.setRoomType(RoomType.PRIVATE);
        return room;
    }

    // 그룹 채팅방 생성용 편의 메서드
    public static ChatRoomEntity createGroupRoom(String roomName) {
        ChatRoomEntity room = new ChatRoomEntity();
        room.setRoomType(RoomType.GROUP);
        room.setRoomName(roomName);
        return room;
    }
}