	package com.group3.askmyfriend.repository;
	
	import com.group3.askmyfriend.entity.SubscriptionEntity;
	import com.group3.askmyfriend.entity.SubscriptionId;
	import org.springframework.data.jpa.repository.JpaRepository;
	import java.util.List;
	
	public interface SubscriptionRepository
	       extends JpaRepository<SubscriptionEntity, SubscriptionId> {
	    // 특정 user가 구독 중인 대상 목록 조회
	    List<SubscriptionEntity> findBySubscriberUserId(Long subscriberId);
	
	    // 특정 대상이 구독자 중인 목록 조회
	    List<SubscriptionEntity> findByTargetUserUserId(Long targetUserId);
	
	    // 구독 여부 확인
	    boolean existsBySubscriberUserIdAndTargetUserUserId(Long subId, Long tgtId);
	
	    // 구독 해제
	    void deleteBySubscriberUserIdAndTargetUserUserId(Long subId, Long tgtId);
	}
