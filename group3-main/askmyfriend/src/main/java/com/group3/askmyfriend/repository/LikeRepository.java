package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.LikeEntity;
import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    int countByPost(PostEntity post);
    boolean existsByPostAndUserEmail(PostEntity post, String userEmail);
    void deleteByPostAndUserEmail(PostEntity post, String userEmail);
}