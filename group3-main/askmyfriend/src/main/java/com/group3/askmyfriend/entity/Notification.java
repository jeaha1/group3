package com.group3.askmyfriend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "target_id")
    private Long targetId;

    @Column(nullable = false, length = 200)
    private String message;

    @Column
    private String url;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    // 기본 생성자
    public Notification() {}

    // Builder 패턴을 위한 생성자
    public Notification(Long id, UserEntity receiver, UserEntity sender, NotificationType type, 
                       Long targetId, String message, String url, LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.targetId = targetId;
        this.message = message;
        this.url = url;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // 모든 getter 메서드
    public Long getId() { return id; }
    public UserEntity getReceiver() { return receiver; }
    public UserEntity getSender() { return sender; }
    public NotificationType getType() { return type; }
    public Long getTargetId() { return targetId; }
    public String getMessage() { return message; }
    public String getUrl() { return url; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // 읽음 상태 관련 getter들
    public boolean isRead() { return isRead; }
    public boolean getRead() { return isRead; }
    public boolean getIsRead() { return isRead; }

    // 모든 setter 메서드
    public void setId(Long id) { this.id = id; }
    public void setReceiver(UserEntity receiver) { this.receiver = receiver; }
    public void setSender(UserEntity sender) { this.sender = sender; }
    public void setType(NotificationType type) { this.type = type; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public void setMessage(String message) { this.message = message; }
    public void setUrl(String url) { this.url = url; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // 읽음 상태 관련 setter들
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    // Builder 패턴 구현
    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private Long id;
        private UserEntity receiver;
        private UserEntity sender;
        private NotificationType type;
        private Long targetId;
        private String message;
        private String url;
        private LocalDateTime createdAt;
        private boolean isRead;

        public NotificationBuilder id(Long id) { this.id = id; return this; }
        public NotificationBuilder receiver(UserEntity receiver) { this.receiver = receiver; return this; }
        public NotificationBuilder sender(UserEntity sender) { this.sender = sender; return this; }
        public NotificationBuilder type(NotificationType type) { this.type = type; return this; }
        public NotificationBuilder targetId(Long targetId) { this.targetId = targetId; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder url(String url) { this.url = url; return this; }
        public NotificationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public NotificationBuilder isRead(boolean isRead) { this.isRead = isRead; return this; }

        public Notification build() {
            return new Notification(id, receiver, sender, type, targetId, message, url, createdAt, isRead);
        }
    }
}
