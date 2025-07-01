package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.ReportRequestDto;
import com.group3.askmyfriend.entity.ReportEntity;
import com.group3.askmyfriend.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {
    private final ReportRepository repo;

    public ReportService(ReportRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Long save(ReportRequestDto dto) {
        ReportEntity r = new ReportEntity();
        // 프론트 author를 실제 신고자 필드에 매핑
        r.setReporter(dto.getAuthor());
        // 프론트 postId를 targetId에
        r.setTargetType("POST");
        r.setTargetId(dto.getPostId());
        // 프론트 reason을 reason 컬럼에
        r.setReason(dto.getReason());
        return repo.save(r).getId();
    }

    @Transactional(readOnly = true)
    public List<ReportEntity> findAll() {
        return repo.findAll();
    }

    @Transactional
    public void updateStatus(Long id, ReportEntity.Status newStatus) {
        ReportEntity report = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 신고가 없습니다. id=" + id));
        report.setStatus(newStatus);
        // 변경 감지에 의해 자동으로 업데이트됩니다.
    }
}
