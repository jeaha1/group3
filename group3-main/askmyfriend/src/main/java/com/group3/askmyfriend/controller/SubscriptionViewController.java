package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.UserRepository;
import com.group3.askmyfriend.service.SubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/subscriptions/view")
public class SubscriptionViewController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepo;

    public SubscriptionViewController(SubscriptionService subscriptionService,
                                      UserRepository userRepo) {
        this.subscriptionService = subscriptionService;
        this.userRepo = userRepo;
    }

    /**
     * 내가 구독한 채널 목록 조회
     * GET /subscriptions/view
     */
    @GetMapping
    public String viewSubscribedChannels(Model model, Principal principal) {
        UserEntity loginUser = userRepo.findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
        List<UserEntity> channels = subscriptionService.getSubscribedChannels(loginUser.getUserId());
        model.addAttribute("channels", channels);
        return "subscribed";    // templates/subscribed.html
    }

    /**
     * 구독하기 액션
     * POST /subscriptions/view/{targetUserId}/subscribe
     */
    @PostMapping("/{targetUserId}/subscribe")
    public String subscribe(@PathVariable Long targetUserId, Principal principal) {
        UserEntity loginUser = userRepo.findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
        subscriptionService.subscribe(loginUser.getUserId(), targetUserId);
        return "redirect:/subscriptions/view";
    }

    /**
     * 구독 해제 액션
     * POST /subscriptions/view/{targetUserId}/unsubscribe
     */
    @PostMapping("/{targetUserId}/unsubscribe")
    public String unsubscribe(@PathVariable Long targetUserId, Principal principal) {
        UserEntity loginUser = userRepo.findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
        subscriptionService.unsubscribe(loginUser.getUserId(), targetUserId);
        return "redirect:/subscriptions/view";
    }

    /**
     * 내 구독자 목록 조회
     * GET /subscriptions/view/subscribers
     */
    @GetMapping("/subscribers")
    public String viewSubscribers(Model model, Principal principal) {
        UserEntity loginUser = userRepo.findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
        List<UserEntity> subscribers = subscriptionService.getSubscribers(loginUser.getUserId());
        model.addAttribute("subscribers", subscribers);
        return "subscribers";   // templates/subscribers.html
    }

    /**
     * 구독자 차단 액션
     * POST /subscriptions/view/{subscriberId}/block
     */
    @PostMapping("/{subscriberId}/block")
    public String blockSubscriber(@PathVariable Long subscriberId, Principal principal) {
        UserEntity loginUser = userRepo.findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));
        subscriptionService.blockSubscriber(loginUser.getUserId(), subscriberId);
        return "redirect:/subscriptions/view/subscribers";
    }
}
	