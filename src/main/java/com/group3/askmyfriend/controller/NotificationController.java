package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.entity.Notification;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.NotificationService;
import com.group3.askmyfriend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(value = "userId", required = false) Long userId, 
                       Model model, Principal principal) {
        
        // 사용자 ID 결정 (파라미터가 없으면 현재 로그인 사용자)
        Long targetUserId = userId;
        if (targetUserId == null && principal != null) {
            UserEntity currentUser = userService.findByLoginId(principal.getName()).orElse(null);
            if (currentUser != null) {
                targetUserId = currentUser.getUserId();
            }
        }
        
        if (targetUserId == null) {
            return "redirect:/login";
        }
        
        // 알림 데이터 조회
        List<Notification> notifications = notificationService.getNotifications(targetUserId);
        long unreadCount = notificationService.getUnreadCount(targetUserId);
        
        // 현재 사용자 정보 추가
        UserEntity currentUser = null;
        if (principal != null) {
            currentUser = userService.findByLoginId(principal.getName()).orElse(null);
        }
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUserId", targetUserId);
        
        return "notification";
    }

    @PutMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable("id") Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<String> markAllAsRead(@RequestParam("userId") Long userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount(@RequestParam("userId") Long userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0L);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteNotification(@PathVariable("id") Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
