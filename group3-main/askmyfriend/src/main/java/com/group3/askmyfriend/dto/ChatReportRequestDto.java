package com.group3.askmyfriend.dto;

public class ChatReportRequestDto {
    private Long messageId;
    private String reason;

    // Getters and Setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}