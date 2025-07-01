package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.dto.CommentDto;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.repository.CommentRepository;
import com.group3.askmyfriend.repository.PostRepository;
import com.group3.askmyfriend.service.PostService;
import com.group3.askmyfriend.service.UserService;
import com.group3.askmyfriend.service.FollowService;
import com.group3.askmyfriend.service.LikeService;
import com.group3.askmyfriend.service.CustomUserDetailsService.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowService followService;

    @Autowired
    private LikeService likeService;

    // ⭐️ 게시물 상세보기 (템플릿 경로 수정)
    @GetMapping("/{postId}")
    public String getPostDetail(@PathVariable Long postId, Model model, @AuthenticationPrincipal CustomUser user) {
        try {
            System.out.println("=== 게시물 상세보기 ===");
            System.out.println("게시물 ID: " + postId);
            
            PostEntity post = postService.findById(postId);
            
            // PostEntity를 PostDto로 변환
            PostDto postDto = new PostDto();
            postDto.setId(post.getId());
            postDto.setContent(post.getContent());
            postDto.setVisibility(post.getVisibility());
            postDto.setPlatform(post.getPlatform());
            postDto.setAccessibility(post.getAccessibility());
            postDto.setImagePath(post.getImagePath());
            postDto.setVideoPath(post.getVideoPath());
            postDto.setLikeCount(likeRepository.countByPost(post));
            postDto.setCommentCount(commentRepository.findByPost(post).size());
            postDto.setShortForm(post.isShortForm());
            postDto.setFormattedTime(formatRelativeTime(post.getCreatedAt()));
            
            // 작성자 정보 추가
            if (post.getAuthor() != null) {
                postDto.setAuthorId(post.getAuthor().getUserId());
                postDto.setAuthorNickname(post.getAuthor().getNickname());
                postDto.setAuthorProfileImg(post.getAuthor().getProfileImg());
                postDto.setAuthorBio(post.getAuthor().getBio());
                System.out.println("작성자: " + post.getAuthor().getNickname());
            } else {
                System.out.println("작성자 정보 없음");
            }
            
            // 댓글 목록 추가
            List<CommentDto> commentDtos = post.getComments().stream()
                    .map(comment -> {
                        CommentDto commentDto = new CommentDto();
                        commentDto.setId(comment.getId());
                        commentDto.setPostId(post.getId());
                        commentDto.setContent(comment.getContent());
                        commentDto.setCreatedAt(comment.getCreatedAt());
                        if (comment.getAuthor() != null) {
                            commentDto.setAuthor(comment.getAuthor().getNickname());
                        } else {
                            commentDto.setAuthor("익명");
                        }
                        return commentDto;
                    })
                    .collect(Collectors.toList());
            
            postDto.setComments(commentDtos);
            System.out.println("댓글 수: " + commentDtos.size());
            
            // 현재 사용자 정보
            if (user != null) {
                UserEntity currentUser = userService.findByLoginId(user.getUsername()).orElse(null);
                model.addAttribute("currentUser", currentUser);
                System.out.println("현재 사용자: " + user.getUsername());
            }
            
            model.addAttribute("post", postDto);
            System.out.println("템플릿 반환: post_detail");
            return "post_detail"; // ⭐️ post_detail.html 템플릿 사용
            
        } catch (Exception e) {
            System.err.println("게시물 상세보기 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        }
    }

    // ⭐️ 댓글 작성 처리 메서드 추가
    @PostMapping("/{postId}/comment")
    public String addComment(@PathVariable Long postId, 
                           @RequestParam String content,
                           Principal principal) {
        try {
            System.out.println("=== 댓글 작성 시도 ===");
            System.out.println("게시물 ID: " + postId + ", 내용: " + content);
            
            if (principal == null) {
                System.out.println("로그인되지 않은 사용자");
                return "redirect:/login";
            }
            
            // 현재 사용자 정보 가져오기
            UserEntity currentUser = userService.findByLoginId(principal.getName()).orElse(null);
            if (currentUser == null) {
                System.out.println("사용자를 찾을 수 없음: " + principal.getName());
                return "redirect:/login";
            }
            
            // 게시물 정보 가져오기
            PostEntity post = postService.findById(postId);
            
            // 댓글 생성
            CommentEntity comment = new CommentEntity();
            comment.setContent(content);
            comment.setPost(post);
            comment.setAuthor(currentUser);
            comment.setCreatedAt(LocalDateTime.now());
            
            // 댓글 저장
            commentRepository.save(comment);
            
            System.out.println("댓글 작성 완료 - 게시물 ID: " + postId + ", 작성자: " + currentUser.getNickname());
            
            // 게시물 상세 페이지로 리다이렉트
            return "redirect:/posts/" + postId;
            
        } catch (Exception e) {
            System.err.println("댓글 작성 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/posts/" + postId;
        }
    }

    // 게시물 목록 - 맞팔로우 필터링 적용
    @GetMapping
    public String showAllPosts(Model model, @AuthenticationPrincipal CustomUser user) {
        List<PostEntity> posts = postService.findAllPosts(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostDto> postDtos = posts.stream()
                .filter(post -> {
                    // 🔥 맞팔로우 필터링 로직
                    if (user == null || post.getAuthor() == null) {
                        return false; // 로그인하지 않았거나 작성자가 없으면 제외
                    }

                    // 본인 게시글은 항상 표시
                    if (post.getAuthor().getUserId().equals(user.getId())) {
                        return true;
                    }

                    // 맞팔로우 상태 실시간 확인
                    return followService.isMutualFollow(user, post.getAuthor().getUserId());
                })
                .map(post -> {
                    PostDto dto = new PostDto();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPlatform(post.getPlatform());
                    dto.setAccessibility(post.getAccessibility());
                    dto.setImagePath(post.getImagePath());
                    dto.setVideoPath(post.getVideoPath());
                    dto.setLikeCount(likeRepository.countByPost(post));
                    dto.setCommentCount(commentRepository.findByPost(post).size());
                    dto.setShortForm(post.isShortForm());

                    // 작성자 정보 추가
                    if (post.getAuthor() != null) {
                        dto.setAuthorId(post.getAuthor().getUserId());
                        dto.setAuthorNickname(post.getAuthor().getNickname());
                        dto.setAuthorProfileImg(post.getAuthor().getProfileImg());
                        dto.setAuthorBio(post.getAuthor().getBio());

                        // 본인 게시글인지 맞팔로우인지 구분
                        if (post.getAuthor().getUserId().equals(user.getId())) {
                            dto.setSubscribed(false); // 본인 게시글은 구독 상태 아님
                        } else {
                            dto.setSubscribed(true); // 맞팔로우 게시글
                        }
                    }

                    // 댓글 목록 추가
                    List<CommentDto> commentDtos = post.getComments().stream()
                            .map(comment -> {
                                CommentDto commentDto = new CommentDto();
                                commentDto.setId(comment.getId());
                                commentDto.setPostId(post.getId());
                                commentDto.setContent(comment.getContent());
                                commentDto.setCreatedAt(comment.getCreatedAt());
                                if (comment.getAuthor() != null) {
                                    commentDto.setAuthor(comment.getAuthor().getNickname());
                                } else {
                                    commentDto.setAuthor("익명");
                                }
                                return commentDto;
                            })
                            .collect(Collectors.toList());

                    dto.setComments(commentDtos);
                    dto.setFormattedTime(formatRelativeTime(post.getCreatedAt()));
                    return dto;
                })
                .collect(Collectors.toList());

        // 현재 로그인 사용자 정보 추가
        if (user != null) {
            UserEntity currentUser = userService.findByLoginId(user.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
        }

        model.addAttribute("posts", postDtos);
        return "posts";
    }

    // 🔥 좋아요 토글 API 추가
    @PostMapping("/{postId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            String userEmail = principal.getName();
            int likeCount = likeService.toggleLike(postId, userEmail);

            // 현재 사용자가 좋아요 했는지 확인
            PostEntity post = postRepository.findById(postId).orElse(null);
            boolean isLiked = post != null &&
                    likeRepository.existsByPostAndUserEmail(post, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);
            response.put("message", isLiked ? "좋아요!" : "좋아요 취소");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 🔥 맞팔로우 피드 (기존과 동일한 로직)
    @GetMapping("/mutual-feed")
    public String showMutualFollowFeed(Model model, @AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return "redirect:/login";
        }

        // 맞팔로우 게시글 조회
        List<PostEntity> mutualPosts = postRepository.findMutualFollowPosts(user.getId());
        List<PostDto> postDtos = mutualPosts.stream()
                .map(post -> {
                    PostDto dto = new PostDto();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPlatform(post.getPlatform());
                    dto.setAccessibility(post.getAccessibility());
                    dto.setImagePath(post.getImagePath());
                    dto.setVideoPath(post.getVideoPath());
                    dto.setLikeCount(likeRepository.countByPost(post));
                    dto.setCommentCount(commentRepository.findByPost(post).size());
                    dto.setShortForm(post.isShortForm());

                    // 작성자 정보 추가
                    if (post.getAuthor() != null) {
                        dto.setAuthorId(post.getAuthor().getUserId());
                        dto.setAuthorNickname(post.getAuthor().getNickname());
                        dto.setAuthorProfileImg(post.getAuthor().getProfileImg());
                        dto.setAuthorBio(post.getAuthor().getBio());
                        dto.setSubscribed(true); // 맞팔로우이므로 true
                    }

                    // 댓글 목록 추가
                    List<CommentDto> commentDtos = post.getComments().stream()
                            .map(comment -> {
                                CommentDto commentDto = new CommentDto();
                                commentDto.setId(comment.getId());
                                commentDto.setPostId(post.getId());
                                commentDto.setContent(comment.getContent());
                                commentDto.setCreatedAt(comment.getCreatedAt());
                                if (comment.getAuthor() != null) {
                                    commentDto.setAuthor(comment.getAuthor().getNickname());
                                } else {
                                    commentDto.setAuthor("익명");
                                }
                                return commentDto;
                            })
                            .collect(Collectors.toList());

                    dto.setComments(commentDtos);
                    dto.setFormattedTime(formatRelativeTime(post.getCreatedAt()));
                    return dto;
                })
                .collect(Collectors.toList());

        // 현재 로그인 사용자 정보 추가
        UserEntity currentUser = userService.findByLoginId(user.getUsername()).orElse(null);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("posts", postDtos);
        return "mutual-follow-feed";
    }

    // 시간 포맷팅 메서드
    private String formatRelativeTime(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        long days = hours / 24;
        if (days < 7) return days + "일 전";
        return createdAt.format(DateTimeFormatter.ofPattern("MM월 dd일"));
    }

    // 게시글 작성 폼
    @GetMapping("/new")
    public String showPostForm(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "post_form";
    }

    // 게시글 작성 처리
    @PostMapping
    public String submitPost(
            @ModelAttribute PostDto postDto,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestParam(value = "shortForm", required = false) Boolean shortForm,
            Principal principal
    ) throws IOException {
        // 현재 로그인한 사용자 정보 가져오기
        if (principal != null) {
            UserEntity currentUser = userService.findByLoginId(principal.getName()).orElse(null);
            if (currentUser != null) {
                // PostDto에 작성자 정보 설정
                postDto.setAuthorId(currentUser.getUserId());
                postDto.setAuthorNickname(currentUser.getNickname());
                postDto.setAuthorProfileImg(currentUser.getProfileImg());
                postDto.setAuthorBio(currentUser.getBio());
            }

            postDto.setShortForm(Boolean.TRUE.equals(shortForm));
            // 작성자 정보와 함께 게시글 생성
            postService.createPost(postDto, imageFile, videoFile, principal);
        }
        return "redirect:/posts";
    }
}
