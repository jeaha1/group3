package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.CommentLikeEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.CommentLikeRepository;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.service.CommentService;
import com.group3.askmyfriend.service.PostService;
import com.group3.askmyfriend.service.SubscriptionService;
import com.group3.askmyfriend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ShortFormController {

    private final PostService postService;
    private final SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public ShortFormController(PostService postService,
                               SubscriptionService subscriptionService) {
        this.postService = postService;
        this.subscriptionService = subscriptionService;
    }

    /** 1) 숏폼 페이지 뷰 - 사용자 정보 추가 */
    @GetMapping("/shorts")
    public String shortsPage(Principal principal, Model model) {
        UserEntity user = null;
        if (principal != null) {
            user = userService.findByLoginId(principal.getName()).orElse(null);
        }
        if (user == null) {
            user = new UserEntity();
            user.setNickname("게스트");
            user.setProfileImg("/static/img/default-avatar.png");
            user.setBio("환영합니다!");
        }
        model.addAttribute("user", user);
        return "shorts";   // templates/shorts.html
    }

    /**
     * 2) 랜덤 숏폼 데이터(JSON) - 작성자/좋아요/댓글 정보 및 구독 상태 추가
     */
    @GetMapping("/shorts/random")
    @ResponseBody
    public PostDto randomShort(Principal principal) {
        PostDto post = postService.getShortForm();
        if (post != null && post.getId() != null) {
            PostEntity postEntity = postService.findById(post.getId());

            // 좋아요·댓글 수
            int likeCount = likeRepository.countByPost(postEntity);
            List<CommentEntity> comments = commentService.getCommentsForPost(post.getId());
            post.setLikeCount(likeCount);
            post.setCommentCount(comments.size());

            // 작성자 정보
            UserEntity author = postEntity.getAuthor();
            if (author != null) {
                post.setAuthorId(author.getUserId());
                post.setAuthorNickname(author.getNickname());
                post.setAuthorProfileImg(
                    author.getProfileImg() != null
                        ? author.getProfileImg()
                        : "/static/img/default-avatar.png"
                );
                post.setAuthorBio(author.getBio());
            } else {
                post.setAuthorId(null);
                post.setAuthorNickname("익명");
                post.setAuthorProfileImg("/static/img/default-avatar.png");
                post.setAuthorBio("작성자 정보가 없습니다.");
            }

            // 🔥 구독 상태: 현재 로그인한 사용자 → 작성자
            boolean isSubscribed = false;
            if (principal != null && author != null) {
                UserEntity me = userService.findByLoginId(principal.getName())
                                           .orElse(null);
                if (me != null) {
                    isSubscribed = subscriptionService.isSubscribed(
                        me.getUserId(),
                        author.getUserId()
                    );
                }
            }
            post.setSubscribed(isSubscribed);
        }
        return post;
    }

    /** 3) 현재 사용자 정보 반환 API - 프로필 이미지 포함 */
    @GetMapping("/api/current-user")
    @ResponseBody
    public Map<String, String> getCurrentUser(Principal principal) {
        Map<String, String> response = new HashMap<>();
        if (principal != null) {
            userService.findByLoginId(principal.getName()).ifPresent(user -> {
                response.put("email", user.getEmail());
                response.put("nickname", user.getNickname());
                response.put("profileImg",
                    user.getProfileImg() != null
                        ? user.getProfileImg()
                        : "/static/img/default-avatar.png"
                );
            });
        }
        return response;
    }

    /** 🔥 새로 추가: 현재 사용자 ID 반환 API */
    @GetMapping("/api/my-user-id")
    @ResponseBody
    public Long getMyUserId(Principal principal) {
        if (principal != null) {
            UserEntity user = userService.findByLoginId(principal.getName()).orElse(null);
            if (user != null) {
                return user.getUserId();
            }
        }
        return null;
    }

    /** 4) 좋아요 상태 확인 API */
    @GetMapping("/likes/status/{postId}")
    @ResponseBody
    public boolean getLikeStatus(@PathVariable Long postId,
                                 @RequestParam String userEmail) {
        try {
            PostEntity post = postService.findById(postId);
            return likeRepository.existsByPostAndUserEmail(post, userEmail);
        } catch (Exception e) {
            return false;
        }
    }

    /** 5) 댓글 목록 가져오기 API */
    @GetMapping("/api/comments/{postId}")
    @ResponseBody
    public List<Map<String, Object>> getComments(@PathVariable Long postId) {
        try {
            return commentService.getCommentsForPost(postId).stream()
                .map(comment -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", comment.getId());
                    m.put("postId", postId);
                    m.put("content", comment.getContent());
                    m.put("createdAt", comment.getCreatedAt());
                    if (comment.getAuthor() != null) {
                        m.put("author", comment.getAuthor().getNickname());
                        m.put("authorProfileImg",
                            comment.getAuthor().getProfileImg() != null
                                ? comment.getAuthor().getProfileImg()
                                : "/static/img/default-avatar.png"
                        );
                    } else {
                        m.put("author", "익명");
                        m.put("authorProfileImg",
                            "/static/img/default-avatar.png"
                        );
                    }
                    return m;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    /** 6) 댓글 작성 API */
    @PostMapping("/api/comments/{postId}")
    @ResponseBody
    public Map<String, Object> addComment(@PathVariable Long postId,
                                          @RequestParam String content,
                                          Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            UserEntity user = userService.findByLoginId(principal.getName())
                                         .orElseThrow();
            var comment = commentService.addComment(postId, content, user.getEmail());
            int count = commentService.getCommentsForPost(postId).size();
            response.put("success", true);
            response.put("commentCount", count);
            response.put("newComment", comment.getContent());
            response.put("author", comment.getAuthor().getNickname());
            response.put("createdAt", comment.getCreatedAt().toString());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "댓글 작성 중 오류: " + e.getMessage());
        }
        return response;
    }

    /** 7) 댓글 좋아요 토글 API */
    @PostMapping("/api/comments/{commentId}/like")
    @ResponseBody
    public Map<String, Object> toggleCommentLike(@PathVariable Long commentId,
                                                 Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            UserEntity user = userService.findByLoginId(principal.getName())
                                         .orElseThrow();
            var comment = commentService.findById(commentId);
            Optional<CommentLikeEntity> existing =
                commentLikeRepository.findByCommentAndUser(comment, user);
            if (existing.isPresent()) {
                commentLikeRepository.delete(existing.get());
                response.put("liked", false);
            } else {
                commentLikeRepository.save(
                    CommentLikeEntity.builder()
                                     .comment(comment)
                                     .user(user)
                                     .build()
                );
                response.put("liked", true);
            }
            int newCount = commentLikeRepository.countByComment(comment);
            response.put("success", true);
            response.put("likeCount", newCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 처리 오류: " + e.getMessage());
        }
        return response;
    }

    /** 8) 댓글 좋아요 상태 확인 API */
    @GetMapping("/api/comments/{commentId}/like-status")
    @ResponseBody
    public Map<String, Object> getCommentLikeStatus(@PathVariable Long commentId,
                                                    Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            var comment = commentService.findById(commentId);
            int likeCount = commentLikeRepository.countByComment(comment);
            response.put("likeCount", likeCount);

            boolean isLiked = false;
            if (principal != null) {
                userService.findByLoginId(principal.getName())
                           .ifPresent(user -> {
                               boolean e = commentLikeRepository
                                              .existsByCommentAndUser(comment, user);
                               response.put("liked", e);
                           });
            }
            response.putIfAbsent("liked", false);
        } catch (Exception e) {
            response.put("liked", false);
            response.put("likeCount", 0);
        }
        return response;
    }
}
