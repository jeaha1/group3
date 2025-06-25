package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.AdminEntity;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.AdminRepository;
import com.group3.askmyfriend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository; // ✅ 회원 조회용 추가
    private final BCryptPasswordEncoder passwordEncoder;

    // 관리자 로그인 인증
    public AdminEntity authenticate(String adminId, String password) {
        AdminEntity admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 ID입니다."));

        if (!admin.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        return admin;
    }

    // ✅ 회원 목록 조회 (회원 관리용)
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
    
    
}
