package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.MypageDto;
import com.group3.askmyfriend.entity.UserEntity;
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

    // 마이페이지 정보 조회
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

        // 팔로잉/팔로워 수 실시간 집계
        int followingCount = followRepository.countByFollower(user);
        int followerCount = followRepository.countByFollowing(user);

        return new MypageDto(
            user.getNickname(),      // username
            user.getLoginId(),       // userId
            user.getBio(),           // 자기소개
            profileImg,              // 프로필 이미지 (기본값 적용)
            backgroundImg,           // 배경 이미지 (기본값 적용)
            followingCount,          // 실시간 집계값
            followerCount,           // 실시간 집계값
            user.getCreatedDate(),
            user.getPrivacy()
        );
    }

 // 본인이 작성한 게시물 목록
    public List<PostEntity> getMyPosts(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            List<PostEntity> posts = postRepository.findByAuthorOrderByCreatedAtDesc(user);
            System.out.println("조회된 게시물 수: " + posts.size());
            
            // 테스트용 더미 데이터 추가
           /* if (posts.isEmpty()) {
                PostEntity dummyPost = new PostEntity();
                dummyPost.setId(999L);
                dummyPost.setContent("테스트 게시물입니다. 실제 게시물을 작성해보세요!");
                posts.add(dummyPost);
            }
            */
            return posts;
        } catch (Exception e) {
            System.err.println("게시물 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 본인이 댓글을 단 게시물 목록
    public List<PostEntity> getMyRepliedPosts(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            return commentRepository.findPostsByReplierOrderByCreatedDateDesc(user);
        } catch (Exception e) {
            System.err.println("답글 게시물 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

 // 본인이 좋아요를 누른 게시물 목록
    public List<PostEntity> getMyLikedPosts(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            // UserEntity의 email 필드를 사용
            String userEmail = user.getEmail();
            return likeRepository.findPostsByUserEmailOrderByIdDesc(userEmail);
        } catch (Exception e) {
            System.err.println("좋아요 게시물 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }


 // 본인이 사진 첨부한 미디어 목록
    public List<PostEntity> getMyMediaList(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            List<PostEntity> media = postRepository.findByAuthorAndImageIsNotNullOrderByCreatedAtDesc(user);
            
            // 테스트용 더미 데이터 추가
           /* if (media.isEmpty()) {
                PostEntity dummyMedia = new PostEntity();
                dummyMedia.setId(998L);
                dummyMedia.setContent("이미지가 첨부된 게시물을 작성해보세요!");
                dummyMedia.setImagePath("/img/profile_default.jpg"); // 테스트용 이미지
                media.add(dummyMedia);
            }
            */
            return media;
        } catch (Exception e) {
            System.err.println("미디어 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
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
