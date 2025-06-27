package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPost(PostEntity post);

    // DISTINCT와 ORDER BY 충돌 해결
    @Query("SELECT DISTINCT c.post FROM CommentEntity c WHERE c.author = :author ORDER BY c.post.createdAt DESC")
    List<PostEntity> findPostsByReplierOrderByCreatedDateDesc(@Param("author") UserEntity author);
}
