package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.FollowEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    // 특정 유저가 특정 유저를 팔로우하고 있는지 확인 (중복 방지용)
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    // 언팔로우 처리
    void deleteByFollowerAndFollowing(UserEntity follower, UserEntity following);

    // 내가 팔로우하는 사람 목록
    List<FollowEntity> findByFollower(UserEntity follower);

    // 나를 팔로우하는 사람 목록
    List<FollowEntity> findByFollowing(UserEntity following);
}
