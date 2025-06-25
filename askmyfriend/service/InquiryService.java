// InquiryService.java
package com.group3.askmyfriend.service;

import com.group3.askmyfriend.entity.InquiryEntity;
import com.group3.askmyfriend.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    // 문의 등록
    public void submitInquiry(InquiryEntity inquiry) {
        inquiryRepository.save(inquiry);
    }

    // 사용자 본인의 문의 목록
    public List<InquiryEntity> getUserInquiries(Long userId) {
        return inquiryRepository.findByUserId(userId);
    }

    // 전체 문의 목록 (관리자용)
    public List<InquiryEntity> getAllInquiries() {
        return inquiryRepository.findAll();
    }

    // 관리자 답변 처리
    public void replyToInquiry(Long id, String reply) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의가 존재하지 않습니다."));

        inquiry.setReply(reply);
        inquiry.setStatus("답변완료");
        inquiry.setAnsweredAt(LocalDateTime.now());

        inquiryRepository.save(inquiry);
    }
}
