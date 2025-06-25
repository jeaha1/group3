package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.UserSummaryDTO;
import com.group3.askmyfriend.entity.FollowEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.FollowRepository;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.service.CustomUserDetailsService.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ 예외처리 포함한 언팔로우 메서드 (트랜잭션 적용)
    @Transactional
    public void unfollow(CustomUser followerUser, Long targetId) {
        try {
            UserEntity follower = userRepository.findById(followerUser.getId())
                .orElseThrow(() -> new RuntimeException("팔로우하는 사용자 없음"));
            UserEntity following = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("언팔로우 대상 사용자 없음"));

            followRepository.deleteByFollowerAndFollowing(follower, following);
        } catch (Exception e) {
            System.err.println("언팔로우 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 예외 재발생
        }
    }

    // ✅ 팔로우 처리 (트랜잭션 적용)
    @Transactional
    public void follow(CustomUser followerUser, Long targetId) {
        if (followerUser.getId().equals(targetId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        UserEntity follower = userRepository.findById(followerUser.getId()).orElseThrow();
        UserEntity following = userRepository.findById(targetId).orElseThrow();

        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.save(new FollowEntity(follower, following));
        }
    }

    // ✅ 내가 팔로우 중인 사용자 목록 (DTO로 반환)
    public List<UserSummaryDTO> getFollowing(CustomUser user) {
        UserEntity me = userRepository.findById(user.getId()).orElseThrow();
        return followRepository.findByFollower(me).stream()
                .map(follow -> new UserSummaryDTO(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    // ✅ 나를 팔로우한 사용자 목록 (DTO로 반환)
    public List<UserSummaryDTO> getFollowers(CustomUser user) {
        UserEntity me = userRepository.findById(user.getId()).orElseThrow();
        return followRepository.findByFollowing(me).stream()
                .map(follow -> new UserSummaryDTO(follow.getFollower()))
                .collect(Collectors.toList());
    }

    // ✅ 맞팔 여부 확인
    public boolean isMutualFollow(CustomUser user, Long otherUserId) {
        UserEntity me = userRepository.findById(user.getId()).orElseThrow();
        UserEntity other = userRepository.findById(otherUserId).orElseThrow();

        boolean iFollowOther = followRepository.existsByFollowerAndFollowing(me, other);
        boolean otherFollowsMe = followRepository.existsByFollowerAndFollowing(other, me);

        return iFollowOther && otherFollowsMe;
    }

    // ✅ 특정 유저에 대해 로그인 유저가 팔로잉 중인지 확인하는 메서드 추가
    public boolean isFollowing(CustomUser user, Long otherUserId) {
        UserEntity me = userRepository.findById(user.getId()).orElseThrow();
        UserEntity other = userRepository.findById(otherUserId).orElseThrow();

        return followRepository.existsByFollowerAndFollowing(me, other);
    }
}
