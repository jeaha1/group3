package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}