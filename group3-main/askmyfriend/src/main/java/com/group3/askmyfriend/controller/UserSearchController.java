package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserSearchController {

    private final UserRepository userRepository;

    @Autowired
    public UserSearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 닉네임 기반 사용자 검색 (부분 일치, 대소문자 무시)
     * 예: /api/user/search?query=민수
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserEntity>> searchUsers(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<UserEntity> users = userRepository.findByNicknameContainingIgnoreCase(query.trim());
        return ResponseEntity.ok(users);
    }
}
