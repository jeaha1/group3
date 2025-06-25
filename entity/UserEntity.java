package com.group3.askmyfriend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long userId;  // 고유 번호 (PK)

    @Column(name = "id", nullable = false, unique = true, length = 50)
    private String loginId;  // 로그인용 아이디

    @Column(nullable = false)
    private String password; // 비밀번호 (암호화됨)

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName; // 사용자 실명

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String status;  // ACTIVE / SUSPENDED / DELETED 등

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(length = 20)
    private String phone;
    
 // === 마이페이지용 필드 추가 ===
    @Column(length = 300)
    private String bio;

    @Column(name = "profile_img", length = 300)
    private String profileImg;

    @Column(name = "background_img", length = 300)
    private String backgroundImg;

    @Column(name = "following_count")
    private int followingCount;

    @Column(name = "follower_count")
    private int followerCount;
    
    @Column(name = "privacy", length = 20)
    private String privacy;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedDate = LocalDateTime.now();
    }
    @Transient
    private String role = "USER";  // DB에 저장되지 않지만 화면에서는 기본값 사용

}