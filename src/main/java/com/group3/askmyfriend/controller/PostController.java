package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.repository.PostRepository;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.service.PostService;
import com.group3.askmyfriend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.group3.askmyfriend.repository.CommentRepository;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository; // ⭐️ 추가

    @Autowired
    private LikeService likeService; // ⭐️ 추가

    // 게시물 목록
    @GetMapping
    public String showAllPosts(Model model) {
        List<PostEntity> posts = postService.findAllPosts(Sort.by(Sort.Direction.DESC, "createdAt"));
        System.out.println("조회된 게시물 수: " + posts.size());

        List<PostDto> postDtos = posts.stream().map(post -> {
            PostDto dto = new PostDto();
            dto.setId(post.getId());
            dto.setContent(post.getContent());
            dto.setVisibility(post.getVisibility());
            dto.setPlatform(post.getPlatform());
            dto.setAccessibility(post.getAccessibility());
            dto.setImagePath(post.getImagePath());
            dto.setLikeCount(likeRepository.countByPost(post));
            dto.setCommentCount(commentRepository.findByPost(post).size());
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("posts", postDtos);
        return "index";
    }

    // 게시글 작성 폼 페이지
    @GetMapping("/new")
    public String showPostForm(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "post_form";
    }

    // 게시글 작성 처리
    @PostMapping
    public String submitPost(@ModelAttribute PostDto postDto,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             Principal principal) throws IOException {
        
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        postService.createPost(postDto, imageFile, principal);
        return "redirect:/";
    }

    // 게시물 상세 보기
    @GetMapping("/{postId}")
    public String showPostDetail(@PathVariable Long postId, Model model) {
        try {
            PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다"));
            
            // 게시물 정보를 모델에 추가
            model.addAttribute("post", post);
            
            // 댓글 목록 추가
            List<CommentEntity> comments = commentRepository.findByPost(post);
            model.addAttribute("comments", comments);
            
            // 좋아요 수 추가
            int likeCount = likeRepository.countByPost(post);
            model.addAttribute("likeCount", likeCount);
            
            System.out.println("게시물 상세 조회: " + post.getId() + ", 댓글 수: " + comments.size());
            
            return "post_detail";
        } catch (Exception e) {
            System.err.println("게시물 상세 조회 오류: " + e.getMessage());
            return "redirect:/";
        }
    }

    // ⭐️ 좋아요 토글 기능 추가
    @PostMapping("/{postId}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@PathVariable Long postId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다");
            return response;
        }
        
        try {
            UserEntity user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            int likeCount = likeService.toggleLike(postId, user.getEmail());
            
            response.put("success", true);
            response.put("likeCount", likeCount);
            System.out.println("좋아요 토글 완료: 게시물 " + postId + ", 좋아요 수: " + likeCount);
            
            return response;
        } catch (Exception e) {
            System.err.println("좋아요 처리 오류: " + e.getMessage());
            response.put("success", false);
            response.put("message", "오류가 발생했습니다");
            return response;
        }
    }

    // ⭐️ 댓글 작성 기능 추가
    @PostMapping("/{postId}/comment")
    public String addComment(@PathVariable Long postId, 
                           @RequestParam String content,
                           Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        try {
            UserEntity user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다"));
            
            CommentEntity comment = new CommentEntity();
            comment.setContent(content);
            comment.setPost(post);
            comment.setAuthor(user);
            
            commentRepository.save(comment);
            
            System.out.println("댓글 작성 완료: 게시물 " + postId + ", 작성자: " + user.getLoginId());
            
        } catch (Exception e) {
            System.err.println("댓글 작성 오류: " + e.getMessage());
        }
        
        return "redirect:/posts/" + postId;
    }
    
    @GetMapping({"/", "/index"})
    public String redirectToPosts(Model model) {
        return showAllPosts(model);
    }
}
