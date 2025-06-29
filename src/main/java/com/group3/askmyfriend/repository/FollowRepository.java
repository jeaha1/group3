package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.FollowEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    // 특정 유저가 특정 유저를 팔로우하고 있는지 확인 (중복 방지용)
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    // 언팔로우 처리
    void deleteByFollowerAndFollowing(UserEntity follower, UserEntity following);

    // 내가 팔로우하는 사람 목록 (FollowEntity 반환)
    List<FollowEntity> findByFollower(UserEntity follower);

    // 나를 팔로우하는 사람 목록 (FollowEntity 반환)
    List<FollowEntity> findByFollowing(UserEntity following);
    
    // ⭐️ FriendController에서 필요한 메서드들 추가
    // 내가 팔로우하는 사람 목록 (UserEntity 리스트 반환)
    @Query("SELECT f.following FROM FollowEntity f WHERE f.follower = :follower")
    List<UserEntity> findFollowingByFollower(@Param("follower") UserEntity follower);

    // 나를 팔로우하는 사람 목록 (UserEntity 리스트 반환)
    @Query("SELECT f.follower FROM FollowEntity f WHERE f.following = :following")
    List<UserEntity> findFollowersByFollowing(@Param("following") UserEntity following);
    
    // ⭐️ 팔로잉/팔로워 숫자 집계 (int → long 변경)
    long countByFollower(UserEntity follower);
    long countByFollowing(UserEntity following);
}
