package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.LikeEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.repository.PostRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional // ✅ 트랜잭션 보장
    public int toggleLike(Long postId, String userEmail) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        boolean alreadyLiked = likeRepository.existsByPostAndUserEmail(post, userEmail);

        if (alreadyLiked) {
            likeRepository.deleteByPostAndUserEmail(post, userEmail); // 여기서 트랜잭션 필요
        } else {
            LikeEntity like = new LikeEntity();
            like.setPost(post);
            like.setUserEmail(userEmail);
            likeRepository.save(like);
        }

        return likeRepository.countByPost(post);
    }

}
