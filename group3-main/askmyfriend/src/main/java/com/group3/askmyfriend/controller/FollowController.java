package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.UserSummaryDTO;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.service.FollowService;
import com.group3.askmyfriend.service.CustomUserDetailsService.CustomUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserRepository userRepository;

    // ✅ 팔로우 요청
    @PostMapping("/{targetUserId}")
    public ResponseEntity<?> follow(@PathVariable Long targetUserId,
                                    @AuthenticationPrincipal CustomUser user) {
        followService.follow(user, targetUserId);
        return ResponseEntity.ok().build();
    }

    // ✅ 언팔로우 요청
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<?> unfollow(@PathVariable Long targetUserId,
                                      @AuthenticationPrincipal CustomUser user) {
        followService.unfollow(user, targetUserId);
        return ResponseEntity.ok().build();
    }

    // ✅ 내가 팔로우한 유저 리스트 (DTO)
    @GetMapping("/following")
    public ResponseEntity<List<UserSummaryDTO>> getFollowing(@AuthenticationPrincipal CustomUser user) {
        List<UserSummaryDTO> list = followService.getFollowing(user);
        return ResponseEntity.ok(list);
    }

    // ✅ 나를 팔로우한 유저 리스트 (DTO)
    @GetMapping("/followers")
    public ResponseEntity<List<UserSummaryDTO>> getFollowers(@AuthenticationPrincipal CustomUser user) {
        List<UserSummaryDTO> list = followService.getFollowers(user);
        return ResponseEntity.ok(list);
    }

    // ✅ 특정 유저와 맞팔 여부 확인
    @GetMapping("/mutual/{otherUserId}")
    public ResponseEntity<Boolean> isMutual(@PathVariable Long otherUserId,
                                            @AuthenticationPrincipal CustomUser user) {
        boolean mutual = followService.isMutualFollow(user, otherUserId);
        return ResponseEntity.ok(mutual);
    }

    // ✅ 특정 유저에 대해 현재 로그인 유저가 팔로잉 중인지 확인 (추가된 API)
    @GetMapping("/isFollowing/{otherUserId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long otherUserId,
                                               @AuthenticationPrincipal CustomUser user) {
        boolean isFollowing = followService.isFollowing(user, otherUserId);
        return ResponseEntity.ok(isFollowing);
    }
}
