package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.LikeEntity;
import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    
    // 게시물별 좋아요 수 조회
    int countByPost(PostEntity post);
    
    // 특정 게시물에 특정 사용자가 좋아요를 눌렀는지 확인
    boolean existsByPostAndUserEmail(PostEntity post, String userEmail);
    
    // 특정 게시물의 특정 사용자 좋아요 삭제
    void deleteByPostAndUserEmail(PostEntity post, String userEmail);
    
    // userEmail 기반으로 좋아요한 게시물 조회
    @Query("SELECT l.post FROM LikeEntity l WHERE l.userEmail = :userEmail ORDER BY l.id DESC")
    List<PostEntity> findPostsByUserEmailOrderByIdDesc(@Param("userEmail") String userEmail);
}
