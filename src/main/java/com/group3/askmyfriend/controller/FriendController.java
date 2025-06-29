package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/friend-management") // ⭐️ 수정: friends-management → friend-management
public class FriendController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FollowRepository followRepository;

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
            
            model.addAttribute("followingList", followingList);
            model.addAttribute("followerList", followerList);
        }
        
        return "friends";
    }
    
    // 친구 검색 API
    @GetMapping("/search")
    @ResponseBody
    public List<UserEntity> searchFriends(@RequestParam String keyword, Principal principal) {
        if (principal == null) {
            return new ArrayList<>();
        }
        
        // 대소문자 구분 없이 닉네임 검색
        List<UserEntity> searchResults = userRepository.findByNicknameContainingIgnoreCase(keyword);
        
        // 본인 제외하고 최대 10명 반환
        String currentLoginId = principal.getName();
        return searchResults.stream()
            .filter(user -> !user.getLoginId().equals(currentLoginId))
            .limit(10)
            .collect(Collectors.toList());
    }
}
