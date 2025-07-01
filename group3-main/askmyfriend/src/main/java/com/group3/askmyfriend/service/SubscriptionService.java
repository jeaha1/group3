package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.SubscriptionEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.SubscriptionRepository;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subsRepo;
    private final UserRepository userRepo;

    public SubscriptionService(SubscriptionRepository subsRepo,
                               UserRepository userRepo) {
        this.subsRepo = subsRepo;
        this.userRepo = userRepo;
    }

    /** 구독 추가 */
    @Transactional
    public void subscribe(Long subscriberId, Long targetUserId) {
        if (subscriberId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신은 구독할 수 없습니다.");
        }
        if (!subsRepo.existsBySubscriberUserIdAndTargetUserUserId(subscriberId, targetUserId)) {
            UserEntity sub = userRepo.findById(subscriberId).orElseThrow();
            UserEntity tgt = userRepo.findById(targetUserId).orElseThrow();
            subsRepo.save(new SubscriptionEntity(sub, tgt));
            sub.setFollowingCount(sub.getFollowingCount() + 1);
            tgt.setFollowerCount(tgt.getFollowerCount() + 1);
            userRepo.save(sub);
            userRepo.save(tgt);
        }
    }

    /** 구독 해제 */
    @Transactional
    public void unsubscribe(Long subscriberId, Long targetUserId) {
        if (subsRepo.existsBySubscriberUserIdAndTargetUserUserId(subscriberId, targetUserId)) {
            subsRepo.deleteBySubscriberUserIdAndTargetUserUserId(subscriberId, targetUserId);
            UserEntity sub = userRepo.findById(subscriberId).orElseThrow();
            UserEntity tgt = userRepo.findById(targetUserId).orElseThrow();
            sub.setFollowingCount(sub.getFollowingCount() - 1);
            tgt.setFollowerCount(tgt.getFollowerCount() - 1);
            userRepo.save(sub);
            userRepo.save(tgt);
        }
    }

    /** 구독 여부 확인 */
    @Transactional(readOnly = true)
    public boolean isSubscribed(Long subscriberId, Long targetUserId) {
        return subsRepo.existsBySubscriberUserIdAndTargetUserUserId(subscriberId, targetUserId);
    }

    /** 내가 구독한 채널 목록 */
    @Transactional(readOnly = true)
    public List<UserEntity> getSubscribedChannels(Long subscriberId) {
        return subsRepo.findBySubscriberUserId(subscriberId).stream()
                .map(SubscriptionEntity::getTargetUser)
                .collect(Collectors.toList());
    }

    /** 내 구독자 목록 */
    @Transactional(readOnly = true)
    public List<UserEntity> getSubscribers(Long targetUserId) {
        return subsRepo.findByTargetUserUserId(targetUserId).stream()
                .map(SubscriptionEntity::getSubscriber)
                .collect(Collectors.toList());
    }

    /** 내 구독자 차단 (구독 해제) */
    @Transactional
    public void blockSubscriber(Long targetUserId, Long subscriberId) {
        unsubscribe(subscriberId, targetUserId);
        // TODO: 블록 이력 저장 로직이 필요하다면 여기에 추가
    }
}
