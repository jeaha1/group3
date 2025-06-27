package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.CommentRepository;
import com.group3.askmyfriend.repository.PostRepository;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository; // ✅ 작성자 정보를 가져오기 위해 필요

    public CommentEntity addComment(Long postId, String content, String authorEmail) {
        PostEntity post = postRepository.findById(postId).orElseThrow();
        UserEntity user = userRepository.findByEmail(authorEmail)
        	    .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 유저를 찾을 수 없습니다."));
        if (user == null) {
            throw new IllegalArgumentException("해당 이메일의 유저를 찾을 수 없습니다: " + authorEmail);
        }

        CommentEntity comment = CommentEntity.builder()
                .content(content)
                .createdAt(LocalDateTime.now())
                .post(post)
                .author(user) // ✅ 작성자 연동
                .build();

        return commentRepository.save(comment);
    }

    public List<CommentEntity> getCommentsForPost(Long postId) {
        PostEntity post = postRepository.findById(postId).orElseThrow();
        return commentRepository.findByPost(post);
    }
}
