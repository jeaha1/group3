package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.LikeEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    
    // 게시물별 좋아요 수 조회
    int countByPost(PostEntity post);
    
    // 특정 게시물에 특정 사용자가 좋아요를 눌렀는지 확인
    boolean existsByPostAndUserEmail(PostEntity post, String userEmail);
    
    // 특정 게시물의 특정 사용자 좋아요 삭제
    void deleteByPostAndUserEmail(PostEntity post, String userEmail);
    
    // ⭐️ userEmail 기반으로 좋아요한 게시물 조회 (마이페이지용)
    @Query("SELECT l.post FROM LikeEntity l WHERE l.userEmail = :userEmail ORDER BY l.id DESC")
    List<PostEntity> findPostsByUserEmailOrderByIdDesc(@Param("userEmail") String userEmail);
    
    // ⭐️ 특정 사용자가 좋아요한 게시물 목록 (최신순)
    @Query("SELECT l.post FROM LikeEntity l WHERE l.userEmail = :userEmail ORDER BY l.post.createdAt DESC")
    List<PostEntity> findPostsByUserEmailOrderByPostCreatedAtDesc(@Param("userEmail") String userEmail);
    
    // ⭐️ 특정 게시물의 좋아요 목록 조회
    List<LikeEntity> findByPost(PostEntity post);
    
    // ⭐️ 특정 게시물의 좋아요 목록 조회 (최신순)
    List<LikeEntity> findByPostOrderByIdDesc(PostEntity post);
    
    // ⭐️ 특정 사용자의 좋아요 목록 조회
    List<LikeEntity> findByUserEmail(String userEmail);
    
    // ⭐️ 특정 사용자의 좋아요 목록 조회 (최신순)
    List<LikeEntity> findByUserEmailOrderByIdDesc(String userEmail);
    
    // ⭐️ 특정 사용자의 총 좋아요 수 계산
    long countByUserEmail(String userEmail);
    
    // ⭐️ 특정 게시물과 사용자의 좋아요 엔티티 조회
    Optional<LikeEntity> findByPostAndUserEmail(PostEntity post, String userEmail);
    
    // ⭐️ 특정 게시물에 좋아요한 사용자 이메일 목록
    @Query("SELECT l.userEmail FROM LikeEntity l WHERE l.post = :post")
    List<String> findUserEmailsByPost(@Param("post") PostEntity post);
    
    // ⭐️ 인기 게시물 조회 (좋아요 수 기준)
    @Query("SELECT l.post, COUNT(l) as likeCount FROM LikeEntity l GROUP BY l.post ORDER BY likeCount DESC")
    List<Object[]> findPopularPosts();
    
    // ⭐️ 특정 기간 내 좋아요 조회 (필요시 사용)
    @Query("SELECT l FROM LikeEntity l WHERE l.userEmail = :userEmail AND l.post.createdAt >= :startDate ORDER BY l.id DESC")
    List<LikeEntity> findByUserEmailAndPostCreatedAtAfter(
        @Param("userEmail") String userEmail, 
        @Param("startDate") java.time.LocalDateTime startDate
    );
}
