package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.CommentDto;
import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    // 게시물 생성
    public void createPost(PostDto dto, MultipartFile imageFile) throws IOException {
        PostEntity entity = new PostEntity();
        entity.setContent(dto.getContent());
        entity.setVisibility(dto.getVisibility());
        entity.setPlatform(dto.getPlatform());
        entity.setAccessibility(dto.getAccessibility());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            entity.setImagePath(imagePath);
        }

        postRepository.save(entity);
    }

    // 이미지 저장
    private String saveImage(MultipartFile file) throws IOException {
        String uploadDir = "uploads";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dirPath = Paths.get(uploadDir);
        Path filePath = dirPath.resolve(fileName);

        Files.createDirectories(dirPath);
        Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE);

        return "/uploads/" + fileName.replace("\\", "/");
    }

    // 게시물 전체 가져오기 (Entity 반환)
    public List<PostEntity> getAllPosts() {
        return postRepository.findAll();
    }

    // 게시물 정렬 포함 조회 (Entity 반환)
    public List<PostEntity> findAllPosts(Sort sort) {
        return postRepository.findAll(sort);
    }

    // ✅ 게시물 + 댓글 + 작성자 닉네임 포함한 DTO 변환
    public List<PostDto> findAllPostDtos(Sort sort) {
        List<PostEntity> posts = postRepository.findAll(sort);
        return posts.stream().map(post -> {
            PostDto dto = new PostDto();
            dto.setId(post.getId());
            dto.setContent(post.getContent());
            dto.setVisibility(post.getVisibility());
            dto.setPlatform(post.getPlatform());
            dto.setAccessibility(post.getAccessibility());
            dto.setImagePath(post.getImagePath());
            dto.setLikeCount(post.getLikes().size());
            dto.setCommentCount(post.getComments().size());

            // 댓글 목록을 CommentDto로 매핑 (nickname 포함)
            List<CommentDto> commentDtos = post.getComments().stream()
                .map(comment -> {
                    CommentDto cdto = new CommentDto();
                    cdto.setId(comment.getId());
                    cdto.setPostId(post.getId());
                    cdto.setContent(comment.getContent());
                    cdto.setCreatedAt(comment.getCreatedAt());

                    if (comment.getAuthor() != null) {
                        cdto.setAuthor(comment.getAuthor().getNickname());
                    } else {
                        cdto.setAuthor("알 수 없음");
                    }

                    return cdto;
                })
                .collect(Collectors.toList());

            dto.setComments(commentDtos);

            return dto;
        }).collect(Collectors.toList());
    }
}
