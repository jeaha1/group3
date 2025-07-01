package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    
    // shortForm=true인 모든 게시물을 조회
    List<PostEntity> findByShortFormTrue();
    
    // ⭐️ MypageService에서 필요한 메서드들 (안전하게 수정)
    
    // 특정 사용자가 작성한 게시물 조회 (작성일 내림차순)
    List<PostEntity> findByAuthorOrderByCreatedAtDesc(UserEntity author);
    
    // ⭐️ 특정 사용자가 작성한 미디어(이미지) 게시물 조회 (@Query로 안전하게 처리)
    @Query("SELECT p FROM PostEntity p WHERE p.author = :author AND p.imagePath IS NOT NULL ORDER BY p.createdAt DESC")
    List<PostEntity> findMediaPostsByAuthor(@Param("author") UserEntity author);
    
    // ⭐️ 특정 사용자가 작성한 미디어(이미지 또는 비디오) 게시물 조회
    @Query("SELECT p FROM PostEntity p WHERE p.author = :author AND (p.imagePath IS NOT NULL OR p.videoPath IS NOT NULL) ORDER BY p.createdAt DESC")
    List<PostEntity> findByAuthorWithMediaOrderByCreatedAtDesc(@Param("author") UserEntity author);
    
    // 🔥 맞팔로우한 사용자들의 게시글 조회 (기존)
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
    
    // ⭐️ 추가: 특정 사용자의 이메일로 작성한 게시물 조회 (필요시)
    @Query("SELECT p FROM PostEntity p WHERE p.author.email = :email ORDER BY p.createdAt DESC")
    List<PostEntity> findByAuthorEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    // ⭐️ 추가: 전체 게시물 조회 (최신순)
    List<PostEntity> findAllByOrderByCreatedAtDesc();
    
    // ⭐️ 추가: 이미지가 있는 모든 게시물 조회 (필요시)
    @Query("SELECT p FROM PostEntity p WHERE p.imagePath IS NOT NULL ORDER BY p.createdAt DESC")
    List<PostEntity> findAllWithImageOrderByCreatedAtDesc();
    
    // ⭐️ 추가: 비디오가 있는 모든 게시물 조회 (필요시)
    @Query("SELECT p FROM PostEntity p WHERE p.videoPath IS NOT NULL ORDER BY p.createdAt DESC")
    List<PostEntity> findAllWithVideoOrderByCreatedAtDesc();
}
