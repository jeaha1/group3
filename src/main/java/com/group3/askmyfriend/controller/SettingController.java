package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.service.UserService;
import com.group3.askmyfriend.service.CustomUserDetailsService.CustomUser;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/setting")
@RequiredArgsConstructor
public class SettingController {

    private final UserService userService;

    @GetMapping
    public String showSettingPage() {
        return "setting/setting"; // templates/setting/setting.html
    }

    @GetMapping("/change-email")
    public String showChangeEmailPage(@AuthenticationPrincipal CustomUser userDetails, Model model) {
        String currentEmail = userDetails.getUsername(); // 로그인 ID로 표시할 경우
        model.addAttribute("currentEmail", currentEmail);
        return "setting/change-email"; // templates/setting/change-email.html
    }

    @GetMapping("/change-email-form")
    public String showChangeEmailFormPage() {
        return "setting/change-email-form"; // templates/setting/change-email-form.html
    }

    @PostMapping("/change-emailProc")
    public String processChangeEmail(@RequestParam("newEmail") String newEmail,
                                     @AuthenticationPrincipal CustomUser userDetails) {

        System.out.println("변경 요청된 이메일: " + newEmail);

        userService.findByLoginId(userDetails.getUsername()).ifPresent(userEntity -> {
            userService.updateEmail(userEntity.getUserId(), newEmail);
        });

        return "redirect:/setting";
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "setting/change-password"; // templates/setting/change-password.html
    }

    // ✅ [추가] 비밀번호 변경 처리
    @PostMapping("/change-passwordProc")
    public String processChangePassword(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        @AuthenticationPrincipal CustomUser userDetails,
                                        Model model) {
        try {
            // 로그인 ID → 사용자 조회 → userId로 비밀번호 변경
            Long userId = userService.findByLoginId(userDetails.getUsername())
                                     .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음."))
                                     .getUserId();

            userService.changePassword(userId, currentPassword, newPassword, confirmPassword);
            return "redirect:/setting"; // 성공 시 설정 페이지로 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "setting/change-password"; // 에러 시 다시 비밀번호 변경 페이지로
        }
    }
    @GetMapping("/change-phone")
    public String showChangePhonePage(@AuthenticationPrincipal CustomUser userDetails, Model model) {
        String loginId = userDetails.getUsername();
        userService.findByLoginId(loginId).ifPresent(user -> {
            model.addAttribute("currentPhone", user.getPhone());
        });
        return "setting/change-phone"; // templates/setting/change-phone.html
    }

    @PostMapping("/change-phoneProc")
    public String processChangePhone(@RequestParam("newPhone") String newPhone,
                                     @AuthenticationPrincipal CustomUser userDetails,
                                     Model model) {
        try {
            Long userId = userService.findByLoginId(userDetails.getUsername())
                                     .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음"))
                                     .getUserId();

            userService.updatePhone(userId, newPhone);
            return "redirect:/setting";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "setting/change-phone";
        }
    }

}
