// InquiryRepository.java
package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {

    // 특정 사용자의 1:1 문의 목록 조회
    List<InquiryEntity> findByUserId(Long userId);
}
