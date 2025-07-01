package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.LikeEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.entity.NotificationType;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.repository.PostRepository;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;  // 알림 서비스 추가

    @Transactional
    public int toggleLike(Long postId, String userEmail) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        boolean alreadyLiked = likeRepository.existsByPostAndUserEmail(post, userEmail);

        if (alreadyLiked) {
            // 좋아요 취소
            likeRepository.deleteByPostAndUserEmail(post, userEmail);
        } else {
            // 좋아요 추가
            LikeEntity like = new LikeEntity();
            like.setPost(post);
            like.setUserEmail(userEmail);
            likeRepository.save(like);
            
            // 🔥 좋아요 알림 생성
            createLikeNotification(post, userEmail);
        }

        return likeRepository.countByPost(post);
    }
    
    /**
     * 좋아요 알림 생성
     */
    private void createLikeNotification(PostEntity post, String likerEmail) {
        try {
            // 자신의 게시물에 좋아요한 경우 알림 생성하지 않음
            if (post.getAuthor().getEmail().equals(likerEmail)) {
                return;
            }
            
            // 좋아요 누른 사용자 정보 조회
            UserEntity liker = userRepository.findByEmail(likerEmail).orElse(null);
            if (liker == null) {
                System.out.println("좋아요 누른 사용자를 찾을 수 없음: " + likerEmail);
                return;
            }
            
            System.out.println("=== 좋아요 알림 생성 ===");
            System.out.println("게시물 작성자: " + post.getAuthor().getNickname());
            System.out.println("좋아요 누른 사용자: " + liker.getNickname());
            
            // 알림 생성
            notificationService.createNotification(
                post.getAuthor().getUserId(),     // 수신자: 게시물 작성자
                liker.getUserId(),                // 발신자: 좋아요 누른 사용자
                NotificationType.LIKE,            // 타입: 좋아요
                post.getId(),                     // 대상: 게시물 ID
                liker.getNickname() + "님이 회원님의 게시물을 좋아합니다.",
                "/posts/" + post.getId()          // 클릭 시 이동할 URL
            );
            
            System.out.println("좋아요 알림 생성 완료!");
            
        } catch (Exception e) {
            System.err.println("좋아요 알림 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
