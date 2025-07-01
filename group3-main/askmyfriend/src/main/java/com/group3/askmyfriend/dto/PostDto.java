package com.group3.askmyfriend.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class PostDto {
    private Long id;
    private String content;
    private String visibility;
    private String platform;
    private String accessibility;
    private String imagePath;

    // 새로 추가된 비디오 파일 & 경로 필드
    private MultipartFile videoFile;
    private String videoPath;
    private Boolean shortForm;

    private long likeCount;
    private long commentCount;

    private List<CommentDto> comments;

    // 🔥 추가: 작성자 정보 필드들
    private Long authorId;           // 🔥 새로 추가된 필드
    private String authorNickname;    // 작성자 닉네임
    private String authorProfileImg;  // 작성자 프로필 이미지
    private String authorBio;         // 작성자 소개

    // 🔥 추가: 구독 상태 필드
    private boolean subscribed;

    // 🔥 추가: 시간 포맷 필드
    private String formattedTime;

    // 기존 Getters and Setters
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

    public MultipartFile getVideoFile() {
        return videoFile;
    }
    public void setVideoFile(MultipartFile videoFile) {
        this.videoFile = videoFile;
    }

    public String getVideoPath() {
        return videoPath;
    }
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public Boolean getShortForm() {
        return shortForm;
    }
    public void setShortForm(Boolean shortForm) {
        this.shortForm = shortForm;
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

    public List<CommentDto> getComments() {
        return comments;
    }
    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    // 🔥 작성자 ID getter/setter (새로 추가)
    public Long getAuthorId() {
        return authorId;
    }
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    // 🔥 작성자 정보 getter/setter
    public String getAuthorNickname() {
        return authorNickname;
    }
    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorProfileImg() {
        return authorProfileImg;
    }
    public void setAuthorProfileImg(String authorProfileImg) {
        this.authorProfileImg = authorProfileImg;
    }

    public String getAuthorBio() {
        return authorBio;
    }
    public void setAuthorBio(String authorBio) {
        this.authorBio = authorBio;
    }

    // 🔥 구독 상태 getter/setter
    public boolean isSubscribed() {
        return subscribed;
    }
    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    // 🔥 시간 포맷 getter/setter (새로 추가)
    public String getFormattedTime() {
        return formattedTime;
    }
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
}
