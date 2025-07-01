package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.MypageDto;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.repository.FollowRepository;
import com.group3.askmyfriend.repository.PostRepository;
import com.group3.askmyfriend.repository.CommentRepository;
import com.group3.askmyfriend.repository.LikeRepository;
import com.group3.askmyfriend.entity.PostEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class MypageService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private LikeRepository likeRepository;

    // ⭐️ 마이페이지 정보 조회 (실시간 팔로우 수 계산 추가)
    public MypageDto getMypageInfo(String loginId) {
        UserEntity user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));

        // 기본 이미지 경로 자동 세팅
        String profileImg = user.getProfileImg();
        if (profileImg == null || profileImg.isEmpty()) {
            profileImg = "/img/profile_default.jpg";
        }
        String backgroundImg = user.getBackgroundImg();
        if (backgroundImg == null || backgroundImg.isEmpty()) {
            backgroundImg = "/img/cover_default.jpg";
        }

        // ⭐️ 실시간 팔로잉/팔로워 수 계산
        int followingCount = 0;
        int followerCount = 0;
        
        try {
            // 내가 팔로우한 사람 수 (팔로잉 수)
            followingCount = (int) followRepository.countByFollower(user);
            // 나를 팔로우한 사람 수 (팔로워 수)
            followerCount = (int) followRepository.countByFollowing(user);
            
            System.out.println("사용자 " + loginId + " - 팔로잉: " + followingCount + ", 팔로워: " + followerCount);
            
        } catch (Exception e) {
            System.err.println("팔로우 수 계산 중 오류: " + e.getMessage());
            // 오류 발생 시 기본값 0 사용
        }

        return new MypageDto(
            user.getLoginId(),       // loginId
            user.getNickname(),      // username
            user.getLoginId(),       // userId (같은 값)
            user.getBio(),           // bio
            profileImg,              // profileImg (기본값 적용)
            backgroundImg,           // backgroundImg (기본값 적용)
            followingCount,          // ⭐️ 실시간 계산된 팔로잉 수
            followerCount,           // ⭐️ 실시간 계산된 팔로워 수
            user.getCreatedDate(),   // createdAt
            user.getPrivacy()        // privacy
        );
    }

    // ⭐️ 본인이 작성한 게시물 목록 (디버깅 강화)
    public List<PostEntity> getMyPosts(String loginId) {
        try {
            System.out.println("=== 게시물 조회 시작 ===");
            System.out.println("조회할 loginId: " + loginId);
            
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            System.out.println("사용자 찾음:");
            System.out.println("- 닉네임: " + user.getNickname());
            System.out.println("- userId: " + user.getUserId());
            System.out.println("- 이메일: " + user.getEmail());
            
            // ⭐️ 먼저 전체 게시물 수 확인
            List<PostEntity> allPosts = postRepository.findAll();
            System.out.println("전체 게시물 수: " + allPosts.size());
            
            // ⭐️ 전체 게시물의 작성자 정보 확인 (최대 5개만)
            int count = 0;
            for (PostEntity post : allPosts) {
                if (count >= 5) break;
                if (post.getAuthor() != null) {
                    System.out.println("게시물 ID: " + post.getId() + 
                                     ", 작성자 userId: " + post.getAuthor().getUserId() + 
                                     ", 작성자 닉네임: " + post.getAuthor().getNickname());
                } else {
                    System.out.println("게시물 ID: " + post.getId() + ", 작성자: null");
                }
                count++;
            }
            
            // ⭐️ 해당 사용자의 게시물 조회
            List<PostEntity> posts = postRepository.findByAuthorOrderByCreatedAtDesc(user);
            System.out.println("조회된 사용자 게시물 수: " + posts.size());
            
            // ⭐️ 조회된 게시물 상세 정보
            for (PostEntity post : posts) {
                System.out.println("- 게시물 ID: " + post.getId() + 
                                 ", 내용: " + (post.getContent() != null ? 
                                              post.getContent().substring(0, Math.min(20, post.getContent().length())) + "..." : "null") + 
                                 ", 작성일: " + post.getCreatedAt());
            }
            
            // ⭐️ 만약 조회된 게시물이 없다면 다른 방법으로 시도
            if (posts.isEmpty()) {
                System.out.println("=== 다른 방법으로 조회 시도 ===");
                List<PostEntity> filteredPosts = allPosts.stream()
                    .filter(post -> post.getAuthor() != null && 
                                   post.getAuthor().getUserId().equals(user.getUserId()))
                    .collect(Collectors.toList());
                System.out.println("필터링된 게시물 수: " + filteredPosts.size());
                
                if (!filteredPosts.isEmpty()) {
                    System.out.println("필터링 방법으로 게시물 찾음!");
                    return filteredPosts;
                }
            }
            
            return posts;
            
        } catch (Exception e) {
            System.err.println("게시물 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ⭐️ 본인이 댓글을 단 게시물 목록 (완전히 새로 작성)
    public List<PostEntity> getMyRepliedPosts(String loginId) {
        try {
            System.out.println("=== 댓글 게시물 조회 시작 ===");
            System.out.println("조회할 loginId: " + loginId);
            
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            System.out.println("사용자 찾음: " + user.getNickname() + " (ID: " + user.getUserId() + ")");
            
            // ⭐️ 1단계: 전체 댓글 확인
            List<CommentEntity> allComments = commentRepository.findAll();
            System.out.println("전체 댓글 수: " + allComments.size());
            
            // ⭐️ 2단계: 해당 사용자가 작성한 댓글 찾기
            List<CommentEntity> userComments = new ArrayList<>();
            for (CommentEntity comment : allComments) {
                if (comment.getAuthor() != null && 
                    comment.getAuthor().getUserId().equals(user.getUserId())) {
                    userComments.add(comment);
                    System.out.println("- 댓글 ID: " + comment.getId() + 
                                     ", 게시물 ID: " + (comment.getPost() != null ? comment.getPost().getId() : "null") +
                                     ", 내용: " + comment.getContent().substring(0, Math.min(10, comment.getContent().length())) + "...");
                }
            }
            System.out.println("사용자가 작성한 댓글 수: " + userComments.size());
            
            // ⭐️ 3단계: 댓글이 달린 게시물 추출 (중복 제거)
            List<PostEntity> repliedPosts = userComments.stream()
                .map(CommentEntity::getPost)
                .filter(post -> post != null)
                .distinct()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
            
            System.out.println("댓글 단 게시물 수: " + repliedPosts.size());
            
            // ⭐️ 4단계: 조회된 게시물 상세 정보
            for (PostEntity post : repliedPosts) {
                System.out.println("- 댓글 단 게시물 ID: " + post.getId() + 
                                 ", 내용: " + (post.getContent() != null ? 
                                              post.getContent().substring(0, Math.min(20, post.getContent().length())) + "..." : "null"));
            }
            
            return repliedPosts;
            
        } catch (Exception e) {
            System.err.println("답글 게시물 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ⭐️ 본인이 좋아요를 누른 게시물 목록 (활성화)
    public List<PostEntity> getMyLikedPosts(String loginId) {
        try {
            System.out.println("=== 좋아요 게시물 조회 시작 ===");
            System.out.println("조회할 loginId: " + loginId);
            
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            System.out.println("사용자 찾음: " + user.getNickname());
            System.out.println("사용자 이메일: " + user.getEmail());
            
            // ⭐️ LikeRepository 메서드 사용
            String userEmail = user.getEmail();
            List<PostEntity> likedPosts = likeRepository.findPostsByUserEmailOrderByIdDesc(userEmail);
            System.out.println("좋아요한 게시물 수: " + likedPosts.size());
            
            // ⭐️ 조회된 게시물 상세 정보
            for (PostEntity post : likedPosts) {
                System.out.println("- 좋아요한 게시물 ID: " + post.getId() + 
                                 ", 내용: " + (post.getContent() != null ? 
                                              post.getContent().substring(0, Math.min(20, post.getContent().length())) + "..." : "null"));
            }
            
            return likedPosts;
            
        } catch (Exception e) {
            System.err.println("좋아요 게시물 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ⭐️ 본인이 사진 첨부한 미디어 목록 (안전한 방법 사용)
    public List<PostEntity> getMyMediaList(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            // ⭐️ 안전한 방법: 모든 게시물 가져온 후 필터링
            List<PostEntity> allPosts = postRepository.findByAuthorOrderByCreatedAtDesc(user);
            
            // 이미지가 있는 게시물만 필터링
            List<PostEntity> media = allPosts.stream()
                .filter(post -> post.getImagePath() != null && !post.getImagePath().isEmpty())
                .collect(Collectors.toList());
            
            System.out.println("조회된 미디어 수: " + media.size());
            return media;
            
        } catch (Exception e) {
            System.err.println("미디어 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ⭐️ 기존 게시물에 작성자 정보 추가 (임시 해결책)
    public void fixExistingPosts() {
        try {
            List<PostEntity> allPosts = postRepository.findAll();
            UserEntity defaultUser = userRepository.findByLoginId("rla840685").orElse(null);
            
            if (defaultUser != null) {
                int fixedCount = 0;
                for (PostEntity post : allPosts) {
                    if (post.getAuthor() == null) {
                        post.setAuthor(defaultUser);
                        postRepository.save(post);
                        fixedCount++;
                        System.out.println("게시물 " + post.getId() + "에 작성자 설정 완료");
                    }
                }
                System.out.println("총 " + fixedCount + "개 게시물 수정 완료");
            } else {
                System.err.println("기본 사용자를 찾을 수 없습니다: rla840685");
            }
        } catch (Exception e) {
            System.err.println("게시물 수정 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ⭐️ 팔로우 수 업데이트 메서드 추가 (팔로우/언팔로우 후 호출용)
    public void updateFollowCounts(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            // 실시간 팔로우 수 계산
            int followingCount = (int) followRepository.countByFollower(user);
            int followerCount = (int) followRepository.countByFollowing(user);
            
            // UserEntity의 팔로우 수 필드 업데이트 (선택사항)
            user.setFollowingCount(followingCount);
            user.setFollowerCount(followerCount);
            userRepository.save(user);
            
            System.out.println("팔로우 수 업데이트 완료 - " + loginId + ": 팔로잉 " + followingCount + ", 팔로워 " + followerCount);
            
        } catch (Exception e) {
            System.err.println("팔로우 수 업데이트 중 오류: " + e.getMessage());
        }
    }

    // 프로필(이미지, 닉네임, 자기소개, 공개범위) 수정
    public void updateProfile(String loginId,
                              MultipartFile backgroundImg,
                              MultipartFile profileImg,
                              String nickname,
                              String bio,
                              String privacy) {
        UserEntity user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));

        // 이미지 저장 경로 (운영 환경에 맞게 조정)
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        System.out.println(uploadDir);
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        // 배경 이미지 저장 (파일명은 UUID+확장자만 사용)
        if (backgroundImg != null && !backgroundImg.isEmpty()) {
            String ext = "";
            String originalName = backgroundImg.getOriginalFilename();
            int dotIdx = originalName.lastIndexOf(".");
            if (dotIdx != -1) {
                ext = originalName.substring(dotIdx);
            }
            String bgFileName = UUID.randomUUID().toString() + ext;
            File bgFile = new File(uploadDir + bgFileName);
            try {
                backgroundImg.transferTo(bgFile);
                user.setBackgroundImg("/uploads/" + bgFileName);
            } catch (IOException e) {
                throw new RuntimeException("배경 이미지 업로드 실패", e);
            }
        }

        // 프로필 이미지 저장 (파일명은 UUID+확장자만 사용)
        if (profileImg != null && !profileImg.isEmpty()) {
            String ext = "";
            String originalName = profileImg.getOriginalFilename();
            int dotIdx = originalName.lastIndexOf(".");
            if (dotIdx != -1) {
                ext = originalName.substring(dotIdx);
            }
            String pfFileName = UUID.randomUUID().toString() + ext;
            File pfFile = new File(uploadDir + pfFileName);
            try {
                profileImg.transferTo(pfFile);
                user.setProfileImg("/uploads/" + pfFileName);
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 업로드 실패", e);
            }
        }

        // 기본 이미지 경로 자동 세팅 (이미지 업로드 안 한 경우)
        if (user.getProfileImg() == null || user.getProfileImg().isEmpty()) {
            user.setProfileImg("/img/profile_default.jpg");
        }
        if (user.getBackgroundImg() == null || user.getBackgroundImg().isEmpty()) {
            user.setBackgroundImg("/img/cover_default.jpg");
        }

        user.setNickname(nickname);
        user.setBio(bio);
        if (privacy != null) user.setPrivacy(privacy);
        user.setModifiedDate(LocalDateTime.now());
        userRepository.save(user);
    }
}
