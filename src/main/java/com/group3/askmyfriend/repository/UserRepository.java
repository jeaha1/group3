package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List; // ← 꼭 필요!

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByUserId(Long userId);
    List<UserEntity> findByNicknameContainingIgnoreCase(String nickname);
}
