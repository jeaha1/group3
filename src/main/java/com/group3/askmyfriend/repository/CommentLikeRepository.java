package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.CommentLikeEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLikeEntity, Long> {
    
    Optional<CommentLikeEntity> findByCommentAndUser(CommentEntity comment, UserEntity user);
    
    boolean existsByCommentAndUser(CommentEntity comment, UserEntity user);
    
    int countByComment(CommentEntity comment);
    
    void deleteByCommentAndUser(CommentEntity comment, UserEntity user);
}
