package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.LoginDTO;
import com.group3.askmyfriend.dto.SignupDTO;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 모바일/앱 전용 엔드포인트
public class MobileAuthController {

    private final UserService userService;

    @Autowired
    public MobileAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDTO signupDTO) {
        try {
            userService.save(signupDTO);
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            UserEntity user = userService.validateLogin(loginDTO.getLoginId(), loginDTO.getPassword());
            // UserEntity 전체 대신 UserDTO 등 필요한 정보만 반환 권장
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("로그인 실패: " + e.getMessage());
        }
    }
}
