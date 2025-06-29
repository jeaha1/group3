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

    // 본인이 작성한 게시물 목록
    public List<PostEntity> getMyPosts(String loginId) {
        try {
            UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + loginId));
            
            List<PostEntity> posts = postRepository.findByAuthorOrderByCreatedAtDesc(user);
            System.out.println("조회된 게시물 수: " + posts.size());
            
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
            
            return media;
        } catch (Exception e) {
            System.err.println("미디어 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
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
