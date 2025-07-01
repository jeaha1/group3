package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    
    // 기존 메서드들
    List<CommentEntity> findByPost(PostEntity post);
    
    // 🔥 누락된 메서드들 추가
    List<CommentEntity> findByAuthor(UserEntity author);
    
    long countByPost(PostEntity post);
    
    // 추가 유용한 메서드들
    List<CommentEntity> findByPostOrderByCreatedAtAsc(PostEntity post);
    
    List<CommentEntity> findByPostOrderByCreatedAtDesc(PostEntity post);
    
    List<CommentEntity> findByAuthorOrderByCreatedAtDesc(UserEntity author);
    
    void deleteByPost(PostEntity post);
    
    void deleteByAuthor(UserEntity author);
}
