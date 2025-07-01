package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.MypageDto;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.repository.FollowRepository;
import com.group3.askmyfriend.service.MypageService;
import com.group3.askmyfriend.service.UserService;
import com.group3.askmyfriend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/friend-management")
public class FriendController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private MypageService mypageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FollowService followService;

    // 친구 관리 페이지
    @GetMapping
    public String friendsPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        String loginId = principal.getName();
        UserEntity currentUser = userRepository.findByLoginId(loginId).orElse(null);
        
        if (currentUser != null) {
            // 팔로잉/팔로워 목록 조회 로직
            List<UserEntity> followingList = followRepository.findFollowingByFollower(currentUser);
            List<UserEntity> followerList = followRepository.findFollowersByFollowing(currentUser);
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("followingList", followingList);
            model.addAttribute("followerList", followerList);
        }
        
        return "friends";
    }
    
    // ⭐️ 친구 페이지로 이동 (비공개 계정 처리 수정)
    @GetMapping("/profile/{loginId}")
    public String viewFriendProfile(@PathVariable String loginId, 
                                  Model model, 
                                  Principal principal) {
        try {
            System.out.println("=== 친구 페이지 이동 ===");
            System.out.println("친구 loginId: " + loginId);
            System.out.println("현재 사용자: " + (principal != null ? principal.getName() : "null"));
            
            // 친구의 사용자 정보 조회
            UserEntity friendUser = userService.findByLoginId(loginId).orElse(null);
            if (friendUser == null) {
                System.out.println("친구를 찾을 수 없음: " + loginId);
                return "redirect:/friend-management";
            }
            
            // 현재 사용자 정보 조회
            UserEntity currentUser = null;
            boolean isOwner = false;
            boolean isFollowing = false;
            
            if (principal != null) {
                String currentLoginId = principal.getName();
                isOwner = currentLoginId.equals(loginId);
                currentUser = userService.findByLoginId(currentLoginId).orElse(null);
                
                if (!isOwner && currentUser != null) {
                    // 팔로우 상태 확인
                    isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, friendUser);
                    System.out.println("팔로우 상태: " + isFollowing);
                }
            }
            
            // ⭐️ 공개범위 체크
            String privacy = friendUser.getPrivacy();
            boolean isPrivate = "private".equals(privacy);
            
            System.out.println("친구 공개범위: " + privacy);
            System.out.println("비공개 계정: " + isPrivate);
            
            // ⭐️ 비공개 계정이고 본인이 아니고 팔로우하지 않은 경우 - 별도 템플릿 사용
            if (isPrivate && !isOwner && !isFollowing) {
                System.out.println("=== 비공개 계정 페이지 렌더링 ===");
                System.out.println("템플릿: mypage_private");
                
                // 비공개 계정 정보만 전달
                MypageDto privateAccountInfo = new MypageDto(
                    friendUser.getLoginId(),
                    friendUser.getNickname(),
                    friendUser.getLoginId(),
                    friendUser.getBio() != null ? friendUser.getBio() : "비공개 계정입니다.",
                    friendUser.getProfileImg() != null ? friendUser.getProfileImg() : "/img/profile_default.jpg",
                    "/img/cover_default.jpg",
                    0, 0, // 팔로우 수 숨김
                    friendUser.getCreatedDate(),
                    "private"
                );
                
                model.addAttribute("user", privateAccountInfo);
                model.addAttribute("isOwner", false);
                model.addAttribute("isFollowing", isFollowing);
                
                System.out.println("비공개 계정 데이터 준비 완료");
                return "mypage_private"; // ⭐️ 별도 템플릿 사용
            }
            
            // ⭐️ 공개 계정이거나 본인이거나 팔로우한 경우 - 정상 표시
            System.out.println("계정 접근 허용 - 공개계정이거나 본인이거나 팔로우함");
            
            MypageDto friendInfo = mypageService.getMypageInfo(loginId);
            List<PostEntity> friendPosts = mypageService.getMyPosts(loginId);
            List<PostEntity> friendMedia = mypageService.getMyMediaList(loginId);
            
            model.addAttribute("user", friendInfo);
            model.addAttribute("posts", friendPosts);
            model.addAttribute("mediaList", friendMedia);
            model.addAttribute("replyList", new ArrayList<>()); // 댓글 탭은 비공개
            model.addAttribute("likePosts", new ArrayList<>()); // 좋아요 탭은 비공개
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isFollowing", isFollowing);
            model.addAttribute("isPrivateAccount", false); // ⭐️ 공개 계정 플래그
            
            System.out.println("친구 페이지 데이터 준비 완료");
            return "mypage"; // 일반 마이페이지 템플릿
            
        } catch (Exception e) {
            System.err.println("친구 페이지 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/friend-management";
        }
    }
    
    // ⭐️ 친구 페이지로 이동 (userId 기반) - 호환성을 위해 추가
    @GetMapping("/profile/user/{userId}")
    public String viewFriendProfileByUserId(@PathVariable Long userId, 
                                          Model model, 
                                          Principal principal) {
        try {
            // userId로 사용자 찾기
            UserEntity friendUser = userRepository.findByUserId(userId).orElse(null);
            if (friendUser == null) {
                return "redirect:/friend-management";
            }
            
            // loginId로 리다이렉트
            return "redirect:/friend-management/profile/" + friendUser.getLoginId();
            
        } catch (Exception e) {
            System.err.println("친구 페이지 조회 오류 (userId): " + e.getMessage());
            return "redirect:/friend-management";
        }
    }
    
    // 친구 검색 API
    @GetMapping("/search")
    @ResponseBody
    public List<UserEntity> searchFriends(@RequestParam String keyword, Principal principal) {
        if (principal == null) {
            return new ArrayList<>();
        }
        
        try {
            // 대소문자 구분 없이 닉네임 검색
            List<UserEntity> searchResults = userRepository.findByNicknameContainingIgnoreCase(keyword);
            
            // 본인 제외하고 최대 10명 반환
            String currentLoginId = principal.getName();
            return searchResults.stream()
                .filter(user -> !user.getLoginId().equals(currentLoginId))
                .limit(10)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("친구 검색 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // ⭐️ 빠른 친구 목록 조회 API
    @GetMapping("/api/following")
    @ResponseBody
    public List<UserEntity> getFollowingList(Principal principal) {
        if (principal == null) {
            return new ArrayList<>();
        }
        
        try {
            UserEntity currentUser = userRepository.findByLoginId(principal.getName()).orElse(null);
            if (currentUser != null) {
                return followRepository.findFollowingByFollower(currentUser);
            }
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("팔로잉 목록 조회 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // ⭐️ 팔로워 목록 조회 API
    @GetMapping("/api/followers")
    @ResponseBody
    public List<UserEntity> getFollowersList(Principal principal) {
        if (principal == null) {
            return new ArrayList<>();
        }
        
        try {
            UserEntity currentUser = userRepository.findByLoginId(principal.getName()).orElse(null);
            if (currentUser != null) {
                return followRepository.findFollowersByFollowing(currentUser);
            }
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("팔로워 목록 조회 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
