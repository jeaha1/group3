package com.group3.askmyfriend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatLogDto {
    private String timestamp;
    private String sender;
    private String receiver; // 현재 구조상 "-" 처리
    private String message;
    private String status;   // "정상" or "신고됨"
    private String reporter; // 예: "자동분석"
}
