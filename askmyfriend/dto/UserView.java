package com.group3.askmyfriend.dto;

import java.time.LocalDateTime; // 🔥 추가

/**
 * 사용자 관리 화면에 뿌릴 최소 정보용 DTO
 * (ProjectMemberEntity.role 기준)
 */
public class UserView {

    private Integer id;
    private String nickname;
    private String email;
    private String role; // 🔥 Enum 말고 String (ADMIN, EDITOR, USER)
    private Integer projectMemberId; // 🔥 추가 (추방용)
    private LocalDateTime createdDate; // 🔥 생성일자 추가

    // 🔥 생성자 수정
    public UserView(Integer id, String nickname, String email, String role, Integer projectMemberId, LocalDateTime createdDate) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.projectMemberId = projectMemberId;
        this.createdDate = createdDate;
    }

    // getter
    public Integer getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Integer getProjectMemberId() {
        return projectMemberId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
