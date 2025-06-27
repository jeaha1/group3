package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    
    // 본인이 작성한 게시물 목록 (게시물 탭용)
    List<PostEntity> findByAuthorOrderByCreatedAtDesc(UserEntity author);
    
    // 본인이 사진 첨부한 게시물 목록 (미디어 탭용)
    @Query("SELECT p FROM PostEntity p WHERE p.author = :author AND p.imagePath IS NOT NULL ORDER BY p.createdAt DESC")
    List<PostEntity> findByAuthorAndImageIsNotNullOrderByCreatedAtDesc(@Param("author") UserEntity author);
}
