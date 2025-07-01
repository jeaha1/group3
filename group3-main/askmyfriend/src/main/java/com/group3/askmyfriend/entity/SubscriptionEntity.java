package com.group3.askmyfriend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @EmbeddedId
    private SubscriptionId id;

    // id.subscriberId 와 매핑될 관계
    @MapsId("subscriberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private UserEntity subscriber;

    // id.targetUserId 와 매핑될 관계
    @MapsId("targetUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private UserEntity targetUser;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt = LocalDateTime.now();

    public SubscriptionEntity() {}

    // 편의 생성자
    public SubscriptionEntity(UserEntity subscriber, UserEntity targetUser) {
        this.subscriber = subscriber;
        this.targetUser = targetUser;
        this.id = new SubscriptionId(subscriber.getUserId(), targetUser.getUserId());
    }

    // ───────────────────────────────────────────────────────────
    // getters / setters

    public SubscriptionId getId() {
        return id;
    }

    public void setId(SubscriptionId id) {
        this.id = id;
    }

    public UserEntity getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(UserEntity subscriber) {
        this.subscriber = subscriber;
    }

    public UserEntity getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(UserEntity targetUser) {
        this.targetUser = targetUser;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }
    // ───────────────────────────────────────────────────────────
}
