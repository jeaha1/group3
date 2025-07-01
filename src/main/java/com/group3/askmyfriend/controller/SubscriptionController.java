package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.SubscriptionService;
import com.group3.askmyfriend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public SubscriptionController(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    /** 🔥 수정: 메인 구독 관리 통합 페이지 */
    @GetMapping
    public String subscriptionPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            UserEntity loginUser = userService.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));

            // 내가 구독한 채널 목록
            List<UserEntity> subscribedChannels = subscriptionService.getSubscribedChannels(loginUser.getUserId());
            
            // 나를 구독한 사용자 목록 (구독자)
            List<UserEntity> mySubscribers = subscriptionService.getSubscribers(loginUser.getUserId());

            model.addAttribute("subscribedChannels", subscribedChannels);
            model.addAttribute("mySubscribers", mySubscribers);
            model.addAttribute("currentUser", loginUser);

            return "subscription-management"; // templates/subscription-management.html
            
        } catch (Exception e) {
            model.addAttribute("error", "구독 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /** 🔥 추가: /manage URL을 메인으로 리다이렉트 */
    @GetMapping("/manage")
    public String redirectToMain() {
        return "redirect:/subscriptions";
    }

    /** 🔥 수정: 구독 해제 - AJAX 응답 */
    @PostMapping("/{targetUserId}/unsubscribe")
    @ResponseBody
    public Map<String, Object> unsubscribe(@PathVariable Long targetUserId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return response;
        }
        
        try {
            UserEntity loginUser = userService.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
            
            // 자기 자신을 구독 취소하려는 경우 방지
            if (loginUser.getUserId().equals(targetUserId)) {
                response.put("success", false);
                response.put("error", "자기 자신을 구독 취소할 수 없습니다.");
                return response;
            }
            
            subscriptionService.unsubscribe(loginUser.getUserId(), targetUserId);
            response.put("success", true);
            response.put("message", "구독이 취소되었습니다.");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /** 🔥 새로 추가: 맞구독 API */
    @PostMapping("/{targetUserId}/subscribe")
    @ResponseBody
    public Map<String, Object> subscribe(@PathVariable Long targetUserId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return response;
        }
        
        try {
            UserEntity loginUser = userService.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
            
            // 자기 자신을 구독하려는 경우 방지
            if (loginUser.getUserId().equals(targetUserId)) {
                response.put("success", false);
                response.put("error", "자기 자신을 구독할 수 없습니다.");
                return response;
            }
            
            // 이미 구독 중인지 확인
            boolean isAlreadySubscribed = subscriptionService.isSubscribed(loginUser.getUserId(), targetUserId);
            if (isAlreadySubscribed) {
                response.put("success", false);
                response.put("error", "이미 구독 중입니다.");
                return response;
            }
            
            subscriptionService.subscribe(loginUser.getUserId(), targetUserId);
            response.put("success", true);
            response.put("message", "구독이 완료되었습니다.");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /** 🔥 수정: 구독자 목록 조회 - 별도 페이지 유지 (필요시) */
    @GetMapping("/subscribers")
    public String viewSubscribers(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            UserEntity loginUser = userService.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));

            List<UserEntity> subscribers = subscriptionService.getSubscribers(loginUser.getUserId());
            model.addAttribute("subscribers", subscribers);
            return "subscriptions/subscribers";
            
        } catch (Exception e) {
            return "redirect:/subscriptions";
        }
    }

    /** 구독자 차단 (구독 해제) */
    @PostMapping("/{subscriberId}/block")
    @ResponseBody
    public Map<String, Object> blockSubscriber(@PathVariable Long subscriberId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return response;
        }
        
        try {
            UserEntity loginUser = userService.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));

            subscriptionService.blockSubscriber(loginUser.getUserId(), subscriberId);
            response.put("success", true);
            response.put("message", "구독자가 차단되었습니다.");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /** 🔥 새로 추가: 구독 상태 확인 API */
    @GetMapping("/{targetUserId}/status")
    @ResponseBody
    public Map<String, Object> getSubscriptionStatus(@PathVariable Long targetUserId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("subscribed", false);
            return response;
        }
        
        try {
            UserEntity loginUser = userService.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
            
            boolean isSubscribed = subscriptionService.isSubscribed(loginUser.getUserId(), targetUserId);
            response.put("subscribed", isSubscribed);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("subscribed", false);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
