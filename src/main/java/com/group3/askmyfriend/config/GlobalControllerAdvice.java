package com.group3.askmyfriend.config;

import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private UserService userService;
    
    /**
     * 모든 Controller에서 사용할 수 있는 전역 사용자 정보
     * 모든 Thymeleaf 템플릿에서 ${user}로 접근 가능
     */
    @ModelAttribute("user")
    public UserEntity getCurrentUser(Principal principal) {
        if (principal != null) {
            try {
                return userService.findByLoginId(principal.getName()).orElse(null);
            } catch (Exception e) {
                // 로그 출력 (선택사항)
                System.err.println("사용자 정보 조회 실패: " + e.getMessage());
                return null;
            }
        }
        return null;
    }
    
    /**
     * 현재 사용자의 로그인 상태 확인
     * 모든 템플릿에서 ${isLoggedIn}으로 접근 가능
     */
    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(Principal principal) {
        return principal != null;
    }
    
    /**
     * 현재 사용자의 이메일 정보
     * 모든 템플릿에서 ${currentUserEmail}로 접근 가능
     */
    @ModelAttribute("currentUserEmail")
    public String getCurrentUserEmail(Principal principal) {
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }
}
