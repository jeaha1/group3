package com.group3.askmyfriend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comments")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    // 🔥 추가: 댓글 좋아요 관계
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CommentLikeEntity> likes = new ArrayList<>();

    // ⭐️ 생성 시간 자동 설정
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // ⭐️ 수정 시간 자동 설정
    @PreUpdate
    protected void onUpdate() {
        // 필요시 수정 시간 필드 추가 가능
    }
    
    // 🔥 추가: 좋아요 수 계산 메서드
    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }
    
    // ⭐️ 편의 생성자 추가 (댓글 작성용)
    public CommentEntity(String content, PostEntity post, UserEntity author) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.likes = new ArrayList<>();
    }
    
    // ⭐️ 댓글 내용 업데이트 메서드
    public void updateContent(String newContent) {
        this.content = newContent;
    }
    
    // ⭐️ 댓글 작성자 확인 메서드
    public boolean isAuthor(UserEntity user) {
        return this.author != null && this.author.getUserId().equals(user.getUserId());
    }
    
    // ⭐️ 댓글이 특정 게시물에 속하는지 확인
    public boolean belongsToPost(PostEntity post) {
        return this.post != null && this.post.getId().equals(post.getId());
    }
    
    // ⭐️ toString 메서드 (디버깅용)
    @Override
    public String toString() {
        return "CommentEntity{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", authorNickname=" + (author != null ? author.getNickname() : "null") +
                ", postId=" + (post != null ? post.getId() : "null") +
                '}';
    }
    
    // ⭐️ equals와 hashCode (JPA 엔티티 비교용)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentEntity)) return false;
        CommentEntity that = (CommentEntity) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
