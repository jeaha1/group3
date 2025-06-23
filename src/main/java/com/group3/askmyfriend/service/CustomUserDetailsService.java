package com.group3.askmyfriend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("계정이 존재하지 않습니다."));

        // 상태 체크
        String status = user.getStatus();
        if ("SUSPENDED".equalsIgnoreCase(status)) {
            throw new InternalAuthenticationServiceException("정지된 계정입니다. 관리자에게 문의하세요.");
        }
        else if ("DELETED".equalsIgnoreCase(status)) {
            throw new InternalAuthenticationServiceException("탈퇴한 계정입니다. 관리자에게 문의하세요.");
        }


        return new CustomUser(user);
    }

    public static class CustomUser extends org.springframework.security.core.userdetails.User {
        private final String nickname;
        private final Long userId;

        public CustomUser(UserEntity user) {
            super(user.getLoginId(), user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            this.nickname = user.getNickname();
            this.userId = user.getUserId();
        }

        public String getNickname() {
            return nickname;
        }

        public Long getId() {
            return userId;
        }
    }
}
