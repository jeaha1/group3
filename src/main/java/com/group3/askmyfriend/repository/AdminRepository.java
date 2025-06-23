package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByAdminId(String adminId);
}
