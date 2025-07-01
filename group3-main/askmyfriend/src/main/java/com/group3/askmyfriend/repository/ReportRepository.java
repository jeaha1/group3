package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    // 필요 시, 중복 신고 방지용 메서드 추가 가능
}
