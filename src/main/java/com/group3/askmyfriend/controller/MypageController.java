package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.MypageDto;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.FollowRepository;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.service.MypageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/mypage")
public class MypageController {

    @Autowired
    private MypageService mypageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    // 본인 마이페이지 GET 요청
    @GetMapping
    public String mypage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        String loginId = principal.getName();
        MypageDto dto = mypageService.getMypageInfo(loginId);
        model.addAttribute("user", dto);
        model.addAttribute("isOwner", true);
        
        // 탭별 데이터 추가
        List<PostEntity> posts = mypageService.getMyPosts(loginId);
        List<PostEntity> replyList = mypageService.getMyRepliedPosts(loginId);
        List<PostEntity> likePosts = mypageService.getMyLikedPosts(loginId);
        List<PostEntity> mediaList = mypageService.getMyMediaList(loginId);
        
        model.addAttribute("posts", posts);
        model.addAttribute("replyList", replyList);
        model.addAttribute("likePosts", likePosts);
        model.addAttribute("mediaList", mediaList);
        
        return "mypage";
    }

    // ⭐️ 다른 사람 마이페이지 보기 (오류 처리 개선)
    @GetMapping("/user/{userId}")
    public String showUserPage(@PathVariable String userId, Principal principal, Model model) {
        try {
            System.out.println("=== 사용자 페이지 접근 시도 ===");
            System.out.println("요청된 userId: " + userId);
            System.out.println("현재 로그인 사용자: " + (principal != null ? principal.getName() : "없음"));
            
            // 해당 사용자 정보 조회
            MypageDto targetUser = mypageService.getMypageInfo(userId);
            System.out.println("대상 사용자 닉네임: " + targetUser.getUsername());
            System.out.println("대상 사용자 공개범위: " + targetUser.getPrivacy());
            
            // 본인이면 본인 마이페이지로 리다이렉트
            if (principal != null && userId.equals(principal.getName())) {
                System.out.println("본인 접근 → 본인 마이페이지로 리다이렉트");
                return "redirect:/mypage";
            }
            
            // ⭐️ 공개범위 확인
            if ("private".equals(targetUser.getPrivacy())) {
                System.out.println("🔒 비공개 계정 접근 감지");
                
                if (principal == null) {
                    System.out.println("❌ 로그인하지 않은 사용자 → mypage_private 페이지");
                    model.addAttribute("user", targetUser);
                    model.addAttribute("isPrivate", true);
                    model.addAttribute("isOwner", false);
                    return "mypage_private";
                }
                
                String currentUserId = principal.getName();
                UserEntity currentUser = userRepository.findByLoginId(currentUserId).orElse(null);
                UserEntity targetUserEntity = userRepository.findByLoginId(userId).orElse(null);
                
                if (currentUser == null || targetUserEntity == null) {
                    System.out.println("❌ 사용자 정보 조회 실패");
                    return "redirect:/";
                }
                
                boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUserEntity);
                System.out.println("팔로우 관계: " + (isFollowing ? "팔로잉 중" : "팔로우 안함"));
                
                if (!isFollowing) {
                    System.out.println("❌ 팔로우하지 않은 사용자 → mypage_private 페이지");
                    model.addAttribute("user", targetUser);
                    model.addAttribute("isPrivate", true);
                    model.addAttribute("isOwner", false);
                    return "mypage_private";
                }
                
                System.out.println("✅ 팔로워 확인됨 → 일반 마이페이지 접근 허용");
            } else {
                System.out.println("🌍 전체공개 계정 → 일반 마이페이지 접근 허용");
            }
            
            // ⭐️ 안전한 모델 데이터 설정
            model.addAttribute("user", targetUser);
            model.addAttribute("isOwner", false);
            
            // 탭별 데이터 조회 (안전하게 처리)
            try {
                List<PostEntity> posts = mypageService.getMyPosts(userId);
                List<PostEntity> replyList = mypageService.getMyRepliedPosts(userId);
                List<PostEntity> likePosts = mypageService.getMyLikedPosts(userId);
                List<PostEntity> mediaList = mypageService.getMyMediaList(userId);
                
                model.addAttribute("posts", posts != null ? posts : new ArrayList<>());
                model.addAttribute("replyList", replyList != null ? replyList : new ArrayList<>());
                model.addAttribute("likePosts", likePosts != null ? likePosts : new ArrayList<>());
                model.addAttribute("mediaList", mediaList != null ? mediaList : new ArrayList<>());
            } catch (Exception e) {
                System.err.println("게시물 데이터 조회 오류: " + e.getMessage());
                // 빈 리스트로 초기화
                model.addAttribute("posts", new ArrayList<>());
                model.addAttribute("replyList", new ArrayList<>());
                model.addAttribute("likePosts", new ArrayList<>());
                model.addAttribute("mediaList", new ArrayList<>());
            }
            
            System.out.println("✅ 정상 접근 → mypage 렌더링 (isOwner: false)");
            return "mypage";
            
        } catch (RuntimeException e) {
            System.err.println("❌ 사용자를 찾을 수 없음: " + userId);
            System.err.println("오류 메시지: " + e.getMessage());
            return "redirect:/?error=user-not-found";
        } catch (Exception e) {
            System.err.println("❌ 예상치 못한 오류 발생");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/?error=unexpected";
        }
    }

    // 프로필 수정 POST 요청
    @PostMapping("/updateProfile")
    public String updateProfile(
            @RequestParam(value = "backgroundImg", required = false) MultipartFile backgroundImg,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestParam("username") String nickname,
            @RequestParam("bio") String bio,
            @RequestParam(value = "privacy", required = false) String privacy,
            Principal principal)
    {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        mypageService.updateProfile(principal.getName(), backgroundImg, profileImg, nickname, bio, privacy);
        return "redirect:/mypage";
    }
}
