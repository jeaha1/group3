package com.group3.askmyfriend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class SignupDTO {
    @NotBlank
    private String loginId;  // 로그인용 ID

    @NotBlank
    private String userName; // 사용자 실명 → 필드명 수정 필요

    @NotBlank
    private String password;

    @NotBlank
    private String passwordConfirm;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String nickname;

    private String phone; // 선택사항

    public void validatePassword() {
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
}

