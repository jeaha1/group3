package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    
    // shortForm=true인 모든 게시물을 조회
    List<PostEntity> findByShortFormTrue();
    
    // 🔥 맞팔로우한 사용자들의 게시글 조회 (새로 추가)
    @Query("SELECT p FROM PostEntity p " +
           "WHERE p.author.userId IN (" +
           "    SELECT f1.following.userId FROM FollowEntity f1 " +
           "    WHERE f1.follower.userId = :currentUserId " +
           "    AND EXISTS (" +
           "        SELECT f2 FROM FollowEntity f2 " +
           "        WHERE f2.follower.userId = f1.following.userId " +
           "        AND f2.following.userId = :currentUserId" +
           "    )" +
           ") " +
           "ORDER BY p.createdAt DESC")
    List<PostEntity> findMutualFollowPosts(@Param("currentUserId") Long currentUserId);
}
