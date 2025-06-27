package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.group3.askmyfriend.repository.CommentRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts") // 모든 경로를 /posts 아래로 정리
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private LikeRepository likeRepository;

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
            dto.setCommentCount(commentRepository.findByPost(post).size()); // ✅ 강제 업데이트
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("posts", postDtos);
        return "index"; // templates/index.html
    }

    // 게시글 작성 폼 페이지
    @GetMapping("/new")
    public String showPostForm(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "post_form"; // templates/post_form.html
    }

    // 게시글 작성 처리
    @PostMapping
    public String submitPost(@ModelAttribute PostDto postDto,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        postService.createPost(postDto, imageFile);
        return "redirect:/";
    }
    @Autowired
    private CommentRepository commentRepository;
    
    @GetMapping({"/", "/index"})
    public String redirectToPosts(Model model) {
        return showAllPosts(model); // 기존 메서드 재사용
    }
}
