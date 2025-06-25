package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.LoginDTO;
import com.group3.askmyfriend.dto.SignupDTO;
import com.group3.askmyfriend.service.AuthService;
import com.group3.askmyfriend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // 회원가입 화면
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid SignupDTO signupDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "signup";
        }

        try {
            signupDTO.validatePassword();
            userService.save(signupDTO);
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    // ✅ 로그인 화면 + 실패 메시지 처리
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            HttpServletRequest request,
                            Model model) {
        if (error != null) {
            Exception ex = (Exception) request.getSession()
                    .getAttribute("SPRING_SECURITY_LAST_EXCEPTION");

            if (ex != null) {
                model.addAttribute("errorMessage", ex.getMessage());
            } else {
                model.addAttribute("errorMessage", "로그인에 실패했습니다.");
            }
        }
        return "login";
    }

    // 이메일 인증 화면
    @GetMapping("/email-verify")
    public String showEmailVerifyPage() {
        return "email-verify";
    }

    // 인증번호 전송
    @PostMapping("/send-code")
    @ResponseBody
    public ResponseEntity<String> sendCode(@RequestParam String email) {
        authService.sendVerificationCode(email);
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    // 인증번호 확인
    @PostMapping("/verify-code")
    @ResponseBody
    public ResponseEntity<Boolean> verifyCode(@RequestParam String email,
                                              @RequestParam String code,
                                              HttpSession session) {
        boolean isValid = authService.verifyCode(email, code);
        if (isValid) {
            session.setAttribute("verifiedEmail", email);
        }
        return ResponseEntity.ok(isValid);
    }

    // 비밀번호 재설정 화면
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String email,
                                        HttpSession session,
                                        Model model) {
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        if (verifiedEmail == null || !verifiedEmail.equals(email)) {
            return "redirect:/auth/login";
        }

        model.addAttribute("email", email);
        return "reset_password";
    }

    // 비밀번호 재설정 처리
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                HttpSession session,
                                Model model) {
        try {
            userService.updatePassword(email, newPassword);
            session.removeAttribute("verifiedEmail");
            return "redirect:/auth/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 변경 실패: " + e.getMessage());
            return "reset_password";
        }
    }

    // 비밀번호 찾기 - 이메일 인증 진입
    @GetMapping("/find-password")
    public String redirectToEmailVerifyPage() {
        return "email-verify";
    }
}
