package com.group3.askmyfriend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "super_admin", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"admin_id"}),
        @UniqueConstraint(columnNames = {"nickname"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false, unique = true, length = 50)
    private String adminId; // 로그인 ID

    @Column(nullable = false, unique = true, length = 50)
    private String nickname; // 관리자 별명 (중복 불가)

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.SUPER_ADMIN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    public enum Role {
        SUPER_ADMIN
    }

    public enum Status {
        ACTIVE, SUSPENDED, DELETED
    }
}
