package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.UserSummaryDTO;
import com.group3.askmyfriend.entity.UserEntity;
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

    // ✅ 팔로우 요청 (기존)
    @PostMapping("/{targetUserId}")
    public ResponseEntity<?> follow(@PathVariable Long targetUserId,
                                    @AuthenticationPrincipal CustomUser user) {
        followService.follow(user, targetUserId);
        return ResponseEntity.ok().build();
    }

    // ✅ 언팔로우 요청 (기존)
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<?> unfollow(@PathVariable Long targetUserId,
                                      @AuthenticationPrincipal CustomUser user) {
        followService.unfollow(user, targetUserId);
        return ResponseEntity.ok().build();
    }

    // ⭐️ 새로 추가: loginId 기반 팔로우 (디버깅 로그 추가)
    @PostMapping("/by-login/{loginId}")
    public ResponseEntity<?> followByLogin(@PathVariable String loginId, 
                                          @AuthenticationPrincipal CustomUser user) {
        try {
            System.out.println("=== 팔로우 시도 ===");
            System.out.println("대상 loginId: " + loginId);
            System.out.println("현재 사용자: " + user.getUsername());
            System.out.println("현재 사용자 ID: " + user.getId());
            
            UserEntity targetUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            System.out.println("대상 사용자 찾음: " + targetUser.getNickname());
            System.out.println("대상 사용자 userId: " + targetUser.getUserId());
            System.out.println("대상 사용자 loginId: " + targetUser.getLoginId());
            
            followService.follow(user, targetUser.getUserId());
            
            System.out.println("팔로우 성공!");
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            System.err.println("팔로우 오류: " + e.getMessage());
            e.printStackTrace(); // ⭐️ 전체 스택 트레이스 출력
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ⭐️ 새로 추가: loginId 기반 언팔로우 (디버깅 로그 추가)
    @DeleteMapping("/by-login/{loginId}")
    public ResponseEntity<?> unfollowByLogin(@PathVariable String loginId,
                                           @AuthenticationPrincipal CustomUser user) {
        try {
            System.out.println("=== 언팔로우 시도 ===");
            System.out.println("대상 loginId: " + loginId);
            System.out.println("현재 사용자: " + user.getUsername());
            
            UserEntity targetUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            System.out.println("대상 사용자 찾음: " + targetUser.getNickname());
            System.out.println("대상 사용자 userId: " + targetUser.getUserId());
            
            followService.unfollow(user, targetUser.getUserId());
            
            System.out.println("언팔로우 성공!");
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            System.err.println("언팔로우 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ⭐️ 새로 추가: loginId 기반 팔로우 상태 확인 (디버깅 로그 추가)
    @GetMapping("/status-by-login/{loginId}")
    public ResponseEntity<Boolean> getFollowStatusByLogin(@PathVariable String loginId,
                                                         @AuthenticationPrincipal CustomUser user) {
        try {
            System.out.println("=== 팔로우 상태 확인 ===");
            System.out.println("대상 loginId: " + loginId);
            System.out.println("현재 사용자: " + user.getUsername());
            
            UserEntity targetUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            System.out.println("대상 사용자 찾음: " + targetUser.getNickname());
            
            boolean isFollowing = followService.isFollowing(user, targetUser.getUserId());
            
            System.out.println("팔로우 상태: " + isFollowing);
            return ResponseEntity.ok(isFollowing);
            
        } catch (Exception e) {
            System.err.println("팔로우 상태 확인 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    // ✅ 내가 팔로우한 유저 리스트 (DTO) - 기존
    @GetMapping("/following")
    public ResponseEntity<List<UserSummaryDTO>> getFollowing(@AuthenticationPrincipal CustomUser user) {
        List<UserSummaryDTO> list = followService.getFollowing(user);
        return ResponseEntity.ok(list);
    }

    // ✅ 나를 팔로우한 유저 리스트 (DTO) - 기존
    @GetMapping("/followers")
    public ResponseEntity<List<UserSummaryDTO>> getFollowers(@AuthenticationPrincipal CustomUser user) {
        List<UserSummaryDTO> list = followService.getFollowers(user);
        return ResponseEntity.ok(list);
    }

    // ✅ 특정 유저와 맞팔 여부 확인 - 기존
    @GetMapping("/mutual/{otherUserId}")
    public ResponseEntity<Boolean> isMutual(@PathVariable Long otherUserId,
                                            @AuthenticationPrincipal CustomUser user) {
        boolean mutual = followService.isMutualFollow(user, otherUserId);
        return ResponseEntity.ok(mutual);
    }

    // ✅ 특정 유저에 대해 현재 로그인 유저가 팔로잉 중인지 확인 - 기존
    @GetMapping("/isFollowing/{otherUserId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long otherUserId,
                                               @AuthenticationPrincipal CustomUser user) {
        boolean isFollowing = followService.isFollowing(user, otherUserId);
        return ResponseEntity.ok(isFollowing);
    }
}
