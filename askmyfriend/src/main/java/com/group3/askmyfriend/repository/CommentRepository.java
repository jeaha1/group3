package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPost(PostEntity post);
}