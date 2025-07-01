package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.AdminEntity;
import com.group3.askmyfriend.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
//각주
    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
        AdminEntity admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 관리자입니다."));

        return User.builder()
        	    .username(admin.getAdminId())
        	    .password(admin.getPassword()) // 평문 비밀번호로 설정
        	    .roles(admin.getRole().name())
        	    .build();

    }
}
