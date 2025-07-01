package com.group3.askmyfriend.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SubscriptionId implements Serializable {
    private Long subscriberId;
    private Long targetUserId;

    public SubscriptionId() {}
    public SubscriptionId(Long subscriberId, Long targetUserId) {
        this.subscriberId = subscriberId;
        this.targetUserId = targetUserId;
    }

    // equals & hashCode: 복합키 비교를 위해 반드시 구현!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionId)) return false;
        SubscriptionId that = (SubscriptionId) o;
        return Objects.equals(subscriberId, that.subscriberId) &&
               Objects.equals(targetUserId, that.targetUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberId, targetUserId);
    }

    // getters / setters
    public Long getSubscriberId() { return subscriberId; }
    public void setSubscriberId(Long subscriberId) { this.subscriberId = subscriberId; }
    public Long getTargetUserId()  { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
}
