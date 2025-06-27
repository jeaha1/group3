package com.group3.askmyfriend.dto;

import java.util.List;

public class PostDto {
    private Long id;
    private String content;
    private String visibility;
    private String platform;
    private String accessibility;
    private String imagePath;
    private long likeCount;
    private long commentCount;

    private List<CommentDto> comments; // ✅ 댓글 리스트 필드 추가

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAccessibility() {
        return accessibility;
    }
    public void setAccessibility(String accessibility) {
        this.accessibility = accessibility;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    // ✅ 댓글 리스트 getter/setter
    public List<CommentDto> getComments() {
        return comments;
    }
    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }
}
