package com.group3.askmyfriend.dto;

public class ReportRequestDto {
    private Long postId;     // 프론트 postId
    private String author;   // 신고자(혹은 신고 대상 작성자)
    private String reason;   // 선택된 신고 사유

    // getters / setters
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
