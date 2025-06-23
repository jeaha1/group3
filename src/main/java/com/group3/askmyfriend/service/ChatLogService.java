package com.group3.askmyfriend.service;

import com.group3.askmyfriend.dto.ChatLogDto;
import com.group3.askmyfriend.entity.ChatMessageEntity;
import com.group3.askmyfriend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatLogService {

    private final ChatMessageRepository chatMessageRepository;

    // 관리자용 로그 전체 조회
    public List<ChatLogDto> getAllLogs() {
        return chatMessageRepository.findAllByIsDeletedFalseOrderBySentAtDesc().stream()
            .map(msg -> {
                String content = msg.getContent();

                // ✅ 욕설/광고 등 키워드 기반 신고 판단
                boolean isReported = content != null && (
                        content.contains("욕") ||
                        content.contains("광고") ||
                        content.contains("비방") ||
                        content.contains("신고") // 필요 시 추가
                );

                return new ChatLogDto(
                    msg.getSentAt().toString(),
                    msg.getSender().getNickname(),
                    "-",  // receiver는 1:1이 아니면 생략
                    content,
                    isReported ? "신고됨" : "정상",
                    isReported ? "자동분석" : ""
                );
            })
            .collect(Collectors.toList());
    }
}
