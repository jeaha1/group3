package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.MypageDto;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MypageService {
    @Autowired
    private UserRepository userRepository;

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

        return new MypageDto(
            user.getNickname(),      // username
            user.getLoginId(),       // userId
            user.getBio(),           // 자기소개
            profileImg,              // 프로필 이미지 (기본값 적용)
            backgroundImg,           // 배경 이미지 (기본값 적용)
            user.getFollowingCount(),
            user.getFollowerCount(),
            user.getCreatedDate(),
            user.getPrivacy()
        );
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

        // 배경 이미지 저장
        if (backgroundImg != null && !backgroundImg.isEmpty()) {
            String bgFileName = UUID.randomUUID() + "_" + backgroundImg.getOriginalFilename();
            File bgFile = new File(uploadDir + bgFileName);
            try {
                backgroundImg.transferTo(bgFile);
                user.setBackgroundImg("/uploads/" + bgFileName); // 웹에서 접근 가능한 경로로 저장
            } catch (IOException e) {
                throw new RuntimeException("배경 이미지 업로드 실패", e);
            }
        }

        // 프로필 이미지 저장
        if (profileImg != null && !profileImg.isEmpty()) {
            String pfFileName = UUID.randomUUID() + "_" + profileImg.getOriginalFilename();
            File pfFile = new File(uploadDir + pfFileName);
            try {
                profileImg.transferTo(pfFile);
                user.setProfileImg("/uploads/" + pfFileName); // 웹에서 접근 가능한 경로로 저장
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
