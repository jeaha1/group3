package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.CommentDto;
import com.group3.askmyfriend.dto.PostDto;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ShortFormSelector shortFormSelector;
    
    // 🔥 추가: UserService 의존성 주입
    @Autowired
    private UserService userService;

    @Autowired
    public PostService(PostRepository postRepository,
                       @Qualifier("randomShortFormSelector") ShortFormSelector shortFormSelector) {
        this.postRepository = postRepository;
        this.shortFormSelector = shortFormSelector;
    }

    // 🔥 추가: ID로 PostEntity 조회
    public PostEntity findById(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
    }

    // ⭐️ 작성자 정보와 함께 게시물 생성 (통합된 메서드)
    public void createPost(PostDto dto,
                           MultipartFile imageFile,
                           MultipartFile videoFile,
                           Principal principal) throws IOException {
        
        System.out.println("=== 게시물 생성 시작 ===");
        System.out.println("내용: " + dto.getContent());
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        
        PostEntity entity = new PostEntity();
        entity.setContent(dto.getContent());
        entity.setVisibility(dto.getVisibility());
        entity.setPlatform(dto.getPlatform());
        entity.setAccessibility(dto.getAccessibility());
        entity.setShortForm(Boolean.TRUE.equals(dto.getShortForm()));

        // ⭐️ 중요: 작성자 정보 설정
        if (principal != null) {
            UserEntity currentUser = userService.findByLoginId(principal.getName()).orElse(null);
            if (currentUser != null) {
                entity.setAuthor(currentUser);
                System.out.println("작성자 설정 완료: " + currentUser.getNickname() + " (ID: " + currentUser.getUserId() + ")");
            } else {
                System.err.println("작성자를 찾을 수 없습니다: " + principal.getName());
            }
        } else {
            System.err.println("Principal이 null입니다 - 로그인 상태 확인 필요");
        }

        // 이미지 저장
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            entity.setImagePath(imagePath);
            System.out.println("이미지 저장: " + imagePath);
        }

        // 비디오 저장
        if (videoFile != null && !videoFile.isEmpty()) {
            String videoPath = saveVideo(videoFile);
            entity.setVideoPath(videoPath);
            System.out.println("비디오 저장: " + videoPath);
        }

        postRepository.save(entity);
        System.out.println("게시물 저장 완료 - ID: " + entity.getId() + 
                          ", 작성자: " + (entity.getAuthor() != null ? entity.getAuthor().getNickname() : "null"));
    }

    // ⭐️ Principal 없는 버전 (호환성을 위해 유지하되 경고 출력)
    public void createPost(PostDto dto,
                           MultipartFile imageFile,
                           MultipartFile videoFile) throws IOException {
        
        System.err.println("⚠️ 경고: 작성자 정보 없이 게시물 생성됨 - Principal을 포함한 메서드를 사용하세요");
        
        PostEntity entity = new PostEntity();
        entity.setContent(dto.getContent());
        entity.setVisibility(dto.getVisibility());
        entity.setPlatform(dto.getPlatform());
        entity.setAccessibility(dto.getAccessibility());
        entity.setShortForm(Boolean.TRUE.equals(dto.getShortForm()));

        // ⭐️ 작성자 정보가 없으므로 null로 저장됨
        System.err.println("작성자 정보 없음 - author 필드가 null로 저장됩니다");

        // 이미지 저장
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            entity.setImagePath(imagePath);
        }

        // 비디오 저장
        if (videoFile != null && !videoFile.isEmpty()) {
            String videoPath = saveVideo(videoFile);
            entity.setVideoPath(videoPath);
        }

        postRepository.save(entity);
        System.out.println("게시물 저장 완료 (작성자 없음) - ID: " + entity.getId());
    }

    // uploads 폴더에 이미지 쓰기. 반환값: "/uploads/{파일명}"
    private String saveImage(MultipartFile file) throws IOException {
        String uploadDir = "uploads";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE);
        return "/uploads/" + fileName.replace("\\", "/");
    }

    // uploads 폴더에 비디오 쓰기. 반환값: "/uploads/{파일명}"
    private String saveVideo(MultipartFile file) throws IOException {
        String uploadDir = "uploads";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE);
        return "/uploads/" + fileName.replace("\\", "/");
    }

    // 게시물 전체 가져오기 (Entity)
    public List<PostEntity> getAllPosts() {
        return postRepository.findAll();
    }

    // 게시물 정렬 포함 조회 (Entity)
    public List<PostEntity> findAllPosts(Sort sort) {
        return postRepository.findAll(sort);
    }

    // 게시물 + 댓글 + 작성자 닉네임 포함한 DTO 변환
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
            dto.setVideoPath(post.getVideoPath());
            dto.setShortForm(post.isShortForm());
            dto.setLikeCount(post.getLikes().size());
            dto.setCommentCount(post.getComments().size());

            // 댓글 목록 매핑
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

    // 숏폼용: shortForm=true인 후보 중 랜덤 선택
    public PostDto getShortForm() {
        List<PostEntity> candidates = postRepository.findByShortFormTrue();
        PostEntity picked = shortFormSelector.selectNext(candidates);
        return toDto(picked);
    }

    // 🔥 개선: 엔티티 → DTO 변환 헬퍼 (좋아요 수 포함)
    private PostDto toDto(PostEntity post) {
        if (post == null) return null;
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setVideoPath(post.getVideoPath());
        dto.setShortForm(post.isShortForm());
        dto.setLikeCount(post.getLikes() != null ? post.getLikes().size() : 0);
        dto.setCommentCount(post.getComments() != null ? post.getComments().size() : 0);
        return dto;
    }
}
