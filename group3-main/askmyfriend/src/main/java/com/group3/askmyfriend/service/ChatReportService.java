package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.ChatReportRequestDto;
import com.group3.askmyfriend.entity.ChatReport;
import com.group3.askmyfriend.entity.UserEntity;
import com.group3.askmyfriend.repository.ChatReportRepository;
import com.group3.askmyfriend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatReportService {

    @Autowired
    private ChatReportRepository chatReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void reportMessage(ChatReportRequestDto dto, Long reporterId) {
        UserEntity reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        ChatReport report = new ChatReport();
        report.setMessageId(dto.getMessageId());
        report.setReason(dto.getReason());
        report.setReporter(reporter);

        chatReportRepository.save(report);
    }
}